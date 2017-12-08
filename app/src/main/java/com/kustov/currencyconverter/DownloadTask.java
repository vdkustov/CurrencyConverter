package com.kustov.currencyconverter;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Vladislav Kusotv on 06.12.2017.
 */

public class DownloadTask extends AsyncTask<String, String, String> {

    private final static String URL_DAILY = "http://www.cbr.ru/scripts/XML_daily.asp";
    private DownloadTaskListener listener;

    public DownloadTask(DownloadTaskListener downloadTaskListener) {
        this.listener = downloadTaskListener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            StringBuilder sb_result = new StringBuilder();
            URL url = new URL(URL_DAILY);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, "windows-1251"));
            String line;
            while ((line = br.readLine()) != null) {
                sb_result.append(line);
            }
            String result = sb_result.toString();
            return (result.isEmpty() ? "" : result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        listener.onDownloadCompleted(result);
    }

}