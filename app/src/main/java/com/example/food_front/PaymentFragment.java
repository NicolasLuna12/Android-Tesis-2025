package com.example.food_front;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.food_front.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private RadioGroup radioGroupPaymentMethod;
    private RadioButton radioButtonCreditCard, radioButtonMercadoPago, radioButtonPayPal;
    private Button buttonNext;
    private static final String PAYMENT_PREFS = "payment_preferences";
    private static final String SELECTED_PAYMENT_METHOD = "selected_payment_method";
    private static final String TAG = "PaymentFragment";    // URLs de los endpoints de MercadoPago
    private static final String BASE_URL = "https://backmobile1.onrender.com"; // Backend principal
    private static final String CREATE_PREFERENCE_URL = BASE_URL + "/payment/create-preference/";
    private static final String HEALTH_CHECK_URL = BASE_URL + "/payment/health/";
    private static final String PAYMENT_STATUS_URL = BASE_URL + "/payment/status/";
    private static final String PAYMENT_REQUEST_URL = BASE_URL + "/payment/request/";
    private static final String WEBHOOK_URL = BASE_URL + "/payment/webhook/";
    
    // Para modo de prueba, usar esta URL si el backend no responde
    private static final String MP_TEST_URL = "https://www.mercadopago.com.ar";
    
    // La PUBLIC_KEY no comienza con TEST-, por lo que es una clave de producción
    private static final boolean USAR_MODO_PRUEBA = false; // Usar el servidor real// Credenciales de Mercado Pago
    private static final String MP_PUBLIC_KEY = "APP_USR-15dcbbb0-ed10-4a65-a8ec-4279e83029a4"; // Clave pública real

    private RequestQueue requestQueue;
    private SessionManager sessionManager;
    private String paymentId;
    private String paymentRequestId;

    public PaymentFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Inicializar RequestQueue y SessionManager
        requestQueue = Volley.newRequestQueue(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Inicializar vistas
        radioButtonCreditCard = view.findViewById(R.id.radioButton4);
        radioButtonMercadoPago = view.findViewById(R.id.radioButton5);
        radioButtonPayPal = view.findViewById(R.id.radioButton6);
        buttonNext = view.findViewById(R.id.button2);

        // Verificar si hay un método de pago previamente seleccionado
        checkPreviousPaymentMethod();

        // Verificar el estado de salud del servicio de pago
        checkPaymentServiceHealth();

        // Configurar el listener del botón siguiente
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePaymentMethodSelection()) {
                    saveSelectedPaymentMethod();
                    processPaymentMethod();
                }
            }
        });

        return view;
    }    /**
     * Verifica el estado de salud del servicio de pago
     */
    private void checkPaymentServiceHealth() {
        if (USAR_MODO_PRUEBA) {
            Log.d(TAG, "Usando modo de prueba. No se verifica el servicio de pago.");
            return;
        }
        
        // Mostrar mensaje informativo
        Toast.makeText(
            requireContext(), 
            "Verificando conexión con el servidor de pagos. Puede tardar un momento...", 
            Toast.LENGTH_LONG
        ).show();
        
        StringRequest request = new StringRequest(
            Request.Method.GET,
            HEALTH_CHECK_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Servicio de pago operativo: " + response);
                    Toast.makeText(requireContext(), "Servidor de pagos conectado correctamente", Toast.LENGTH_SHORT).show();
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error al verificar el estado del servicio de pago", error);
                    
                    // El servidor puede estar "despertando", no mostrar error inmediatamente
                    Toast.makeText(
                        requireContext(), 
                        "El servidor de pagos está iniciando. Puede tardar hasta 60 segundos en estar disponible.", 
                        Toast.LENGTH_LONG
                    ).show();
                }
            }
        );
        
        // Ampliar el timeout para dar tiempo al servidor a "despertar"
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
            30000, // 30 segundos de timeout
            2,     // 2 reintentos
            1.5f   // Backoff multiplier para espaciar los reintentos
        ));

        requestQueue.add(request);
    }

    /**
     * Verifica si hay un método de pago previamente seleccionado y lo marca
     */
    private void checkPreviousPaymentMethod() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
        String savedPaymentMethod = prefs.getString(SELECTED_PAYMENT_METHOD, "");

        if (!savedPaymentMethod.isEmpty()) {
            switch (savedPaymentMethod) {
                case "credit_card":
                    radioButtonCreditCard.setChecked(true);
                    break;
                case "mercado_pago":
                    radioButtonMercadoPago.setChecked(true);
                    break;
                case "paypal":
                    radioButtonPayPal.setChecked(true);
                    break;
            }
        }
    }

    /**
     * Valida que el usuario haya seleccionado un método de pago
     */
    private boolean validatePaymentMethodSelection() {
        if (!radioButtonCreditCard.isChecked() &&
            !radioButtonMercadoPago.isChecked() &&
            !radioButtonPayPal.isChecked()) {

            Toast.makeText(requireContext(), "Por favor, seleccione un método de pago", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Guarda el método de pago seleccionado en SharedPreferences
     */
    private void saveSelectedPaymentMethod() {
        String selectedMethod = "";

        if (radioButtonCreditCard.isChecked()) {
            selectedMethod = "credit_card";
        } else if (radioButtonMercadoPago.isChecked()) {
            selectedMethod = "mercado_pago";
        } else if (radioButtonPayPal.isChecked()) {
            selectedMethod = "paypal";
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PAYMENT_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SELECTED_PAYMENT_METHOD, selectedMethod);
        editor.apply();
    }

    /**
     * Procesa el método de pago seleccionado
     */
    private void processPaymentMethod() {
        if (radioButtonCreditCard.isChecked()) {
            processCreditCardPayment();
        } else if (radioButtonMercadoPago.isChecked()) {
            processMercadoPagoPayment();
        } else if (radioButtonPayPal.isChecked()) {
            processPayPalPayment();
        }
    }

    /**
     * Procesa el pago con tarjeta de crédito
     */
    private void processCreditCardPayment() {
        Toast.makeText(requireContext(), "Procesando pago con Tarjeta de Crédito", Toast.LENGTH_SHORT).show();
        replaceFragment(new SuccessFragment());
    }

    /**
     * Procesa el pago con PayPal
     */
    private void processPayPalPayment() {
        Toast.makeText(requireContext(), "Procesando pago con PayPal", Toast.LENGTH_SHORT).show();
        replaceFragment(new SuccessFragment());
    }    /**
     * Procesa el pago con MercadoPago
     */
    private void processMercadoPagoPayment() {
        Toast.makeText(requireContext(), "Iniciando proceso de pago con MercadoPago...", Toast.LENGTH_SHORT).show();

        // La clave proporcionada es una clave de producción (no tiene el prefijo TEST-)
        Log.d(TAG, "Usando clave pública de producción: " + MP_PUBLIC_KEY);
        
        // Crear la preferencia de pago en MercadoPago
        createMercadoPagoPreference();
    }/**
     * Crea una preferencia de pago en MercadoPago
     */
    private void createMercadoPagoPreference() {
        // Mostrar mensaje de carga
        Toast.makeText(requireContext(), "Preparando pago con MercadoPago...", Toast.LENGTH_SHORT).show();

        // Verificar que tengamos una clave pública válida
        if (MP_PUBLIC_KEY == null || MP_PUBLIC_KEY.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No se ha configurado la clave pública de MercadoPago", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log para verificar la clave que estamos usando
        Log.d(TAG, "Creando preferencia con clave pública: " + MP_PUBLIC_KEY);

        // Obtener token de sesión
        String userToken = sessionManager.getToken();
        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No se encontró un token de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Primero, obtener los productos del carrito desde el servidor
        obtenerProductosCarrito(userToken);
    }    /**
     * Obtiene los productos del carrito desde el servidor
     */
    private void obtenerProductosCarrito(final String userToken) {
        // URL correcta del endpoint del carrito
        String urlCarrito = "https://backmobile1.onrender.com/appCART/ver/";  // URL corregida

        // Mostrar mensaje de espera
        final ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Obteniendo productos del carrito...\nEsto puede tardar hasta un minuto si el servidor estaba inactivo.");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET,
            urlCarrito,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Ocultar diálogo de progreso
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                
                    try {
                        Log.d(TAG, "Respuesta del carrito: " + response.toString());

                        // Verificar si el carrito está vacío
                        if (!response.has("carrito") || response.getJSONArray("carrito").length() == 0) {
                            Toast.makeText(requireContext(), "El carrito está vacío. Agregue productos antes de continuar.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Procesar la respuesta y crear la preferencia de pago
                        crearPreferenciaConProductosCarrito(userToken, response);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar la respuesta del carrito", e);
                        Toast.makeText(requireContext(), "Error al obtener los productos del carrito", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Ocultar diálogo de progreso
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                
                    // Si el error es timeout, dar un mensaje específico
                    if (error instanceof com.android.volley.TimeoutError) {
                        Toast.makeText(
                            requireContext(), 
                            "El servidor tarda en responder. Por favor, inténtelo nuevamente en unos segundos.", 
                            Toast.LENGTH_LONG
                        ).show();
                    } else {
                        handleApiError(error, "Error al obtener los productos del carrito");
                    }
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Usar 'Token' en vez de 'Bearer' según el requerimiento del backend
                headers.put("Authorization", "Token " + userToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
          // Configurar tiempo de espera más largo
        jsonObjectRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
            60000, // 60 segundos de timeout
            1,     // 1 reintento
            1.5f   // Con backoff multiplier
        ));
        
        // Enviar la solicitud
        requestQueue.add(jsonObjectRequest);

        requestQueue.add(jsonObjectRequest);
    }    /**
     * Procesa la respuesta del carrito y crea la preferencia de pago
     */
    private void crearPreferenciaConProductosCarrito(String userToken, JSONObject carritoResponse) {
        try {
            // Crear un JSONObject con los datos para la preferencia
            JSONObject preferenceObj = new JSONObject();
            
            // Extraer datos del carrito
            JSONArray carritoItems = carritoResponse.getJSONArray("carrito");
            JSONArray items = new JSONArray();
            
            double totalAmount = 0.0;
            
            // Procesar cada item del carrito
            for (int i = 0; i < carritoItems.length(); i++) {
                JSONObject cartItem = carritoItems.getJSONObject(i);
                JSONObject item = new JSONObject();
                
                JSONObject producto = cartItem.getJSONObject("producto");
                int cantidad = cartItem.getInt("cantidad");
                double precio = producto.getDouble("precio");
                String nombre = producto.getString("nombre");
                String descripcion = producto.optString("descripcion", "Producto de Food Front");
                
                item.put("title", nombre);
                item.put("description", descripcion);
                item.put("quantity", cantidad);
                item.put("unit_price", precio);
                
                items.put(item);
                totalAmount += precio * cantidad;
            }
            
            preferenceObj.put("items", items);
            
            // Crear back_urls
            JSONObject backUrls = new JSONObject();
            backUrls.put("success", "https://backmobile1.onrender.com/payment/success");
            backUrls.put("failure", "https://backmobile1.onrender.com/payment/failure");
            backUrls.put("pending", "https://backmobile1.onrender.com/payment/pending");
            preferenceObj.put("back_urls", backUrls);
            
            preferenceObj.put("auto_return", "approved");
            
            // Agregar información adicional para el webhook si es necesario
            preferenceObj.put("notification_url", WEBHOOK_URL);
            
            // Enviar la solicitud al servidor para crear la preferencia
            enviarPreferenciaMercadoPago(userToken, preferenceObj);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error al crear la preferencia", e);
            Toast.makeText(requireContext(), "Error al procesar los productos del carrito", Toast.LENGTH_SHORT).show();
        }
    }    /**
     * Envía la preferencia de pago a MercadoPago
     */
    private void enviarPreferenciaMercadoPago(final String userToken, JSONObject preferenceObj) {
        // Mostrar un mensaje de que se está procesando
        Toast.makeText(requireContext(), "Conectando con MercadoPago...", Toast.LENGTH_SHORT).show();
        
        // Mostrar un diálogo de progreso mientras se espera al servidor
        final ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Conectando con el servidor de pagos. Puede tardar hasta un minuto...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        try {
            // Agregar la clave pública de Mercado Pago al objeto de la preferencia
            preferenceObj.put("public_key", MP_PUBLIC_KEY);
            
            // Agregar metadatos adicionales si es necesario
            JSONObject metadata = new JSONObject();
            metadata.put("app_version", "1.0.0");
            metadata.put("platform", "android");
            preferenceObj.put("metadata", metadata);
            
            Log.d(TAG, "Enviando preferencia: " + preferenceObj.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error al preparar la preferencia", e);
        }
        
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.POST,
            CREATE_PREFERENCE_URL,
            preferenceObj,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {                    try {
                        // Asegurarnos de cerrar el diálogo de progreso
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        
                        Log.d(TAG, "Respuesta de preferencia de MercadoPago: " + response.toString());
                        
                        // Verificar si la respuesta contiene el init_point o sandbox_init_point
                        if (response.has("init_point") || response.has("sandbox_init_point")) {
                            // Obtener el URL para el checkout
                            String checkoutUrl = response.has("init_point") 
                                ? response.getString("init_point") 
                                : response.getString("sandbox_init_point");
                            
                            // Guardar el ID de la preferencia si está disponible
                            if (response.has("id")) {
                                paymentId = response.getString("id");
                            }
                            
                            // Iniciar el WebView para completar el pago
                            iniciarWebViewParaPago(checkoutUrl);
                        } else {
                            Toast.makeText(requireContext(), "Error: No se recibió un URL de pago válido", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar la respuesta de preferencia", e);
                        Toast.makeText(requireContext(), "Error al procesar la respuesta de MercadoPago", Toast.LENGTH_SHORT).show();
                        
                        // Asegurarnos de cerrar el diálogo de progreso en caso de error
                        if (progressDialog != null && progressDialog.isShowing()) {
                            try {
                                progressDialog.dismiss();
                            } catch (Exception ex) {
                                // Ignorar
                            }
                        }
                    }
                }
            },
            new Response.ErrorListener() {                @Override
                public void onErrorResponse(VolleyError error) {
                    // Asegurarnos de cerrar el diálogo de progreso
                    if (progressDialog != null && progressDialog.isShowing()) {
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            // Ignorar si el fragmento ya no está adjunto
                        }
                    }
                    
                    Log.e(TAG, "Error al crear preferencia de pago", error);
                    handleApiError(error, "Error al conectar con MercadoPago");
                }
            }
        ) {            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + userToken);
                headers.put("Content-Type", "application/json");
                headers.put("X-MP-Public-Key", MP_PUBLIC_KEY); // Incluir la clave pública en los headers
                return headers;
            }
        };
        
        // Aumentar el timeout para dar más tiempo al servidor que puede estar "despertando"
        jsonObjectRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
            60000, // 60 segundos de timeout
            2,     // 2 reintentos (total 3 intentos)
            1.5f   // Backoff multiplier para espaciar los reintentos
        ));// Agregar la solicitud a la cola
        requestQueue.add(jsonObjectRequest);
        
        // Configurar un handler para cerrar el diálogo de progreso después de un tiempo
        // en caso de que la solicitud no retorne correctamente
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    try {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "La conexión está tardando más de lo esperado, intenta nuevamente", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // Ignorar errores si el fragmento no está adjunto
                    }
                }
            }
        }, 70000); // 70 segundos, justo después del tiempo de espera de la solicitud
    }
    
    /**
     * Inicia el WebView para completar el pago en MercadoPago
     */
    private void iniciarWebViewParaPago(String checkoutUrl) {
        Log.d(TAG, "Iniciando WebView con URL: " + checkoutUrl);
        Toast.makeText(requireContext(), "Redirigiendo al checkout de MercadoPago...", Toast.LENGTH_SHORT).show();
        
        // Crear intent para la actividad del WebView
        Intent intent = new Intent(requireContext(), MercadoPagoWebViewActivity.class);
        intent.putExtra(MercadoPagoWebViewActivity.EXTRA_PAYMENT_URL, checkoutUrl);
        
        // Iniciar la actividad esperando un resultado
        startActivityForResult(intent, 1001); // Código de solicitud arbitrario
    }    /**
     * Maneja el resultado del proceso de pago en el WebView
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001) {
            // El usuario regresó del WebView de MercadoPago
            // Verificar el estado del pago
            if (paymentId != null) {
                verificarEstadoPago(paymentId);
            } else {
                Toast.makeText(requireContext(), "No se pudo determinar el estado del pago", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Verifica el estado del pago usando el ID de la preferencia
     */
    private void verificarEstadoPago(String preferenceId) {
        String userToken = sessionManager.getToken();
        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No hay un token de usuario", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String statusUrl = PAYMENT_STATUS_URL + preferenceId;
        
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET,
            statusUrl,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(TAG, "Respuesta del estado del pago: " + response.toString());
                        
                        String status = response.optString("status", "unknown");
                        
                        switch (status.toLowerCase()) {
                            case "approved":
                                // Pago aprobado
                                Toast.makeText(requireContext(), "¡Pago aprobado!", Toast.LENGTH_SHORT).show();
                                // Navegar a la pantalla de éxito
                                replaceFragment(new SuccessFragment());
                                break;
                                
                            case "pending":
                                // Pago pendiente
                                Toast.makeText(requireContext(), "El pago está pendiente de confirmación", Toast.LENGTH_LONG).show();
                                break;
                                
                            case "rejected":
                                // Pago rechazado
                                Toast.makeText(requireContext(), "El pago fue rechazado", Toast.LENGTH_SHORT).show();
                                break;
                                
                            default:
                                // Estado desconocido
                                Toast.makeText(requireContext(), "Estado del pago: " + status, Toast.LENGTH_SHORT).show();
                                break;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al procesar la respuesta del estado del pago", e);
                        Toast.makeText(requireContext(), "Error al verificar el estado del pago", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error al verificar el estado del pago", error);
                    handleApiError(error, "Error al verificar el estado del pago");
                    Toast.makeText(requireContext(), "No fue posible verificar el estado del pago", Toast.LENGTH_SHORT).show();
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + userToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Maneja errores de las solicitudes API
     */
    private void handleApiError(VolleyError error, String message) {
        Log.e(TAG, message, error);
        String errorMsg = message;
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            errorMsg += " (Código: " + statusCode + ")";
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                Log.e(TAG, "Error response body: " + responseBody);
            } catch (Exception e) {
                Log.e(TAG, "Error al leer el cuerpo del error", e);
            }
        } else {
            errorMsg += " - Revise su conexión a Internet";
        }
        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
    }

    private void replaceFragment(Fragment newFragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, newFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
