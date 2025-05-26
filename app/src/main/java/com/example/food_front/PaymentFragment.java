package com.example.food_front;

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
    private static final String TAG = "PaymentFragment";

    // URLs de los endpoints de MercadoPago
    private static final String BASE_URL = "https://backmp.onrender.com";
    private static final String CREATE_PREFERENCE_URL = BASE_URL + "/payment/create-preference/";
    private static final String HEALTH_CHECK_URL = BASE_URL + "/payment/health/";
    private static final String PAYMENT_STATUS_URL = BASE_URL + "/payment/status/";
    private static final String PAYMENT_REQUEST_URL = BASE_URL + "/payment/request/";
    private static final String WEBHOOK_URL = BASE_URL + "/payment/webhook/";

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
    }

    /**
     * Verifica el estado de salud del servicio de pago
     */
    private void checkPaymentServiceHealth() {
        StringRequest request = new StringRequest(
            Request.Method.GET,
            HEALTH_CHECK_URL,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Servicio de pago operativo: " + response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error al verificar el estado del servicio de pago", error);
                    Toast.makeText(requireContext(), "El servicio de pago podría no estar disponible", Toast.LENGTH_SHORT).show();
                }
            }
        );

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
    }

    /**
     * Procesa el pago con MercadoPago
     */
    private void processMercadoPagoPayment() {
        Toast.makeText(requireContext(), "Iniciando proceso de pago con MercadoPago...", Toast.LENGTH_SHORT).show();

        // Crear la preferencia de pago en MercadoPago
        createMercadoPagoPreference();
    }

    /**
     * Crea una preferencia de pago en MercadoPago
     */
    private void createMercadoPagoPreference() {
        // Mostrar mensaje de carga
        Toast.makeText(requireContext(), "Preparando pago con MercadoPago...", Toast.LENGTH_SHORT).show();

        // Obtener token de sesión
        String userToken = sessionManager.getToken();
        if (userToken == null || userToken.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No se encontró un token de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        // Primero, obtener los productos del carrito desde el servidor
        obtenerProductosCarrito(userToken);
    }

    /**
     * Obtiene los productos del carrito desde el servidor
     */
    private void obtenerProductosCarrito(final String userToken) {
        // URL correcta del endpoint del carrito
        String urlCarrito = "https://backmobile1.onrender.com/appCART/ver/";  // URL corregida

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            Request.Method.GET,
            urlCarrito,
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
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
                    handleApiError(error, "Error al obtener los productos del carrito");
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

        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Procesa la respuesta del carrito y crea la preferencia de pago
     */
    private void crearPreferenciaConProductosCarrito(String userToken, JSONObject carritoResponse) {
        // Implementa aquí la lógica para crear la preferencia de pago usando los productos del carrito
        // Por ejemplo, puedes reutilizar el código que ya tenías para armar el JSON de preferencia y llamar a MercadoPago
        // Si necesitas el código exacto, avísame
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
