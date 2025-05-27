package com.example.food_front;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MercadoPagoWebViewActivity extends AppCompatActivity {
    public static final String EXTRA_PAYMENT_URL = "extra_payment_url";
    private WebView webView;
    private ProgressDialog progressDialog;
    private static final String TAG = "MercadoPagoWebView";
    
    // URLs que indican fin del proceso de pago
    private static final String[] SUCCESS_URLS = {
        "success", "aprobado", "approved", "congratulations"
    };
    
    private static final String[] FAILURE_URLS = {
        "failure", "error", "rejected", "cancelled"
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview); // Usamos un layout dedicado
        
        // Configurar ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Checkout MercadoPago");
        }
        
        // Inicializar el WebView
        webView = findViewById(R.id.webview);
        
        // Crear y mostrar el diálogo de progreso
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando el checkout de MercadoPago...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String paymentUrl = getIntent().getStringExtra(EXTRA_PAYMENT_URL);
        if (paymentUrl == null) {
            Toast.makeText(this, "Error: No se proporcionó una URL de pago", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configuración del WebView
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                // Actualizar la barra de progreso
                if (newProgress < 100) {
                    if (progressDialog.isShowing()) {
                        progressDialog.setMessage("Cargando... " + newProgress + "%");
                    }
                } else {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }
        });
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                Log.d(TAG, "Navegando a URL: " + url);
                
                // Verificar si es una URL de éxito o fracaso
                if (containsAny(url, SUCCESS_URLS)) {
                    Log.d(TAG, "URL de éxito detectada");
                    setResult(RESULT_OK);
                    finish();
                    return true;
                } else if (containsAny(url, FAILURE_URLS)) {
                    Log.d(TAG, "URL de fracaso detectada");
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                
                // Permitir la navegación normal
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                
                // Mostrar error al usuario
                Toast.makeText(MercadoPagoWebViewActivity.this, 
                    "Error al cargar la página de pago", Toast.LENGTH_LONG).show();
            }
        });
        
        // Cargar la URL
        webView.loadUrl(paymentUrl);
        Log.d(TAG, "Iniciando carga de URL: " + paymentUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Preguntar al usuario si realmente desea cancelar el pago
            Toast.makeText(this, "Proceso de pago cancelado", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }
    
    /**
     * Verifica si una URL contiene alguna de las palabras clave
     */
    private boolean containsAny(String url, String[] keywords) {
        for (String keyword : keywords) {
            if (url.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}

