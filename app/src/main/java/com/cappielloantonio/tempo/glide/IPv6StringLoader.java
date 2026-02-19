package com.cappielloantonio.tempo.glide;

import androidx.annotation.NonNull;
import android.os.SystemClock;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IPv6StringLoader implements ModelLoader<String, InputStream> {
    private static final int DEFAULT_TIMEOUT_MS = 1200;
    private static final long FAILURE_CACHE_TTL_MS = 5 * 60 * 1000L;
    private static final Map<String, Long> failedRequests = new ConcurrentHashMap<>();

    @Override
    public boolean handles(@NonNull String model) {
        return model.startsWith("http://") || model.startsWith("https://");
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull String model, int width, int height, @NonNull Options options) {
        if (!handles(model)) {
            return null;
        }
        return new LoadData<>(new ObjectKey(model), new IPv6StreamFetcher(model));
    }

    private static class IPv6StreamFetcher implements DataFetcher<InputStream> {
        private final String model;
        private InputStream stream;
        private HttpURLConnection connection;

        IPv6StreamFetcher(String model) {
            this.model = model;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
            Long failureTimestamp = failedRequests.get(model);
            long now = SystemClock.elapsedRealtime();
            if (failureTimestamp != null) {
                if ((now - failureTimestamp) < FAILURE_CACHE_TTL_MS) {
                    callback.onLoadFailed(new IOException("Skipping recently failed image request"));
                    return;
                }
                failedRequests.remove(model);
            }

            try {
                URL url = new URL(model);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
                connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
                connection.setUseCaches(true);
                connection.setDoInput(true);
                connection.connect();

                if (connection.getResponseCode() / 100 != 2) {
                    failedRequests.put(model, SystemClock.elapsedRealtime());
                    callback.onLoadFailed(new IOException("Request failed with status code: " + connection.getResponseCode()));
                    return;
                }

                stream = connection.getInputStream();
                failedRequests.remove(model);
                callback.onDataReady(stream);
            } catch (IOException e) {
                failedRequests.put(model, SystemClock.elapsedRealtime());
                callback.onLoadFailed(e);
            }
        }

        @Override
        public void cleanup() {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        @Override
        public void cancel() {
            // HttpURLConnection does not provide a direct cancel mechanism.
        }

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.REMOTE;
        }
    }

    public static class Factory implements ModelLoaderFactory<String, InputStream> {
        @NonNull
        @Override
        public ModelLoader<String, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new IPv6StringLoader();
        }

        @Override
        public void teardown() {
            // No-op
        }
    }
}
