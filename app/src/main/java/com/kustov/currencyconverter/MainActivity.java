package com.kustov.currencyconverter;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kustov.currencyconverter.simplexml.ValCurs;
import com.kustov.currencyconverter.simplexml.Valute;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements DownloadTaskListener, View.OnClickListener {

    private AsyncTask<String, String, String> downloadTask;
    private Context context;
    private TextView tv_date;
    private EditText et_input, et_output;
    private Button btn_convert, btn_reset;
    private Spinner spn_from, spn_to;
    private DSP dsp;
    private String inputted;
    private ValCurs data;
    private ProgressBar progressBar;

    private void init() {
        context = getBaseContext();
        tv_date = findViewById(R.id.tv_date);
        et_input = findViewById(R.id.et_input);
        et_output = findViewById(R.id.et_output);
        btn_convert = findViewById(R.id.btn_convert);
        btn_reset = findViewById(R.id.btn_reset);
        spn_from = findViewById(R.id.spn_from);
        spn_to = findViewById(R.id.spn_to);
        progressBar = findViewById(R.id.progressBar);

        btn_convert.setOnClickListener(this);
        btn_reset.setOnClickListener(this);
        dsp = new DSP(context); //Default Shared Preferences

        inputted = dsp.getString("inputted", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        et_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Save inputted value to shared preferences
                dsp.setString("inputted", charSequence.toString());
                if (charSequence.toString().isEmpty()) {
                    btn_reset.setEnabled(false);
                } else {
                    btn_reset.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        et_input.setText(inputted);
        updateCurrency();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_convert:
                String input = et_input.getText().toString();
                if (!input.isEmpty()) {
                    et_output.setText(convert(input));
                } else {
                    echo(R.string.msg_empty);
                }
                break;
            case R.id.btn_reset:
                et_input.setText("");
                et_output.setText("");
                dsp.setString("inputted", "");
                break;
        }
    }

    /**
     * Convert process
     *
     * @param input - initial value of choosed currency
     * @return output - result
     */
    private String convert(String input) {
        //Get positions of spinners
        int pos_from = spn_from.getSelectedItemPosition();
        int pos_to = spn_to.getSelectedItemPosition();

        //Get currency values using spinners positions
        double curr_from;
        double curr_from_nominal;
        double curr_to;
        double curr_to_nominal;

        //Check is choosed last item. Last item = RUB
        int last = spn_from.getAdapter().getCount() - 1;
        if (pos_from == last && pos_to == last) {
            return input;
        } else if (pos_from == last && pos_to != last) {
            curr_from = 1;
            curr_from_nominal = 1;
            curr_to = toDouble(data.valutes.get(pos_to).value);
            curr_to_nominal = toDouble(data.valutes.get(pos_to).nominal);
        } else if (pos_from != last && pos_to == last) {
            curr_from = toDouble(data.valutes.get(pos_from).value);
            curr_from_nominal = toDouble(data.valutes.get(pos_from).nominal);
            curr_to = 1;
            curr_to_nominal = 1;
        } else {
            curr_from = toDouble(data.valutes.get(pos_from).value);
            curr_from_nominal = toDouble(data.valutes.get(pos_from).nominal);
            curr_to = toDouble(data.valutes.get(pos_to).value);
            curr_to_nominal = toDouble(data.valutes.get(pos_to).nominal);
        }

        //Calculate result and return
        double output = (curr_from * toDouble(input) / curr_to) / curr_from_nominal * curr_to_nominal;
        return (output != 0.0) ? new DecimalFormat("0.0000").format(output) : "";
    }

    /**
     * Trying to upload latest info from server.
     * Anyway restore data from offline.
     */
    private void updateCurrency() {
        boolean isOfflineDataExists = restoreOfflineData();
        if (!isOfflineDataExists) {
            btn_convert.setEnabled(false);
            et_input.setEnabled(false);
        }
        if (isConnection()) {
            progressBar.setVisibility(View.VISIBLE);
            downloadTask = new DownloadTask(this).execute();
        } else {
            echo(R.string.msg_no_connection);
        }
    }

    @Override
    public void onDownloadCompleted(String result) {
        progressBar.setVisibility(View.GONE);
        if (!result.isEmpty()) {
            btn_convert.setEnabled(true);
            et_input.setEnabled(true);
            dsp.setString("xml_data", result);
            data = parseXML(result);
            updateCurrencyLists(data.valutes);
            updateDate(data.Date);
        }
    }

    private boolean restoreOfflineData() {
        String restored = dsp.getString("xml_data", "");
        if (!restored.isEmpty()) {
            data = parseXML(restored);
            updateCurrencyLists(data.valutes);
            updateDate(data.Date);
            return true;
        }
        return false;
    }

    /**
     * Update latest date in label
     */
    private void updateDate(String date) {
        tv_date.setText(getResources().getString(R.string.tv_date).replace("$DATE", date));
    }

    /**
     * Update spinners content using parsed data xml.
     *
     * @param list - parsed data xml
     */
    private void updateCurrencyLists(List<Valute> list) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add("(" + list.get(i).charCode + "): " + list.get(i).name);
        }
        //Add Russian RUB
        result.add(getResources().getString(R.string.spn_last_item));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                result);

        spn_from.setAdapter(adapter);
        spn_to.setAdapter(adapter);

        spn_from.setSelection(dsp.getInt("position_from", 0)); //Restore position
        spn_from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dsp.setInt("position_from", i);
                updateOutput();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spn_to.setSelection(dsp.getInt("position_to", 0)); //Restore position
        spn_to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dsp.setInt("position_to", i);
                updateOutput();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Parse xml data using SimpleXML
     */
    @Nullable
    private ValCurs parseXML(String xml) {
        Serializer serializer = new Persister();
        Reader reader = new StringReader(xml);
        try {
            return serializer.read(ValCurs.class, reader, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert currency again. Refresh the result.
     */
    private void updateOutput() {
        if (!et_input.getText().toString().isEmpty()) {
            this.onClick(findViewById(R.id.btn_convert));
        }
    }

    @Nullable
    private Double toDouble(String value) {
        if (!value.isEmpty()) {
            return Double.parseDouble(value.replace(",", "."));
        }
        return 0.0;
    }

    private boolean isConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void echo(int id) {
        Toast.makeText(context, getResources().getString(id), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_refresh) {
            updateCurrency();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
    }

}