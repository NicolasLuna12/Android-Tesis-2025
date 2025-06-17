package com.example.food_front;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_front.utils.DashboardHelper;
import com.example.food_front.utils.SessionManager;


public class SuccessFragment extends Fragment {

    private static final String TAG = "SuccessFragment";
    private TextView textMessage;
    private String paymentRequestId;
    private String paymentMethod;

    public SuccessFragment() {
        // Required empty public constructor
    }
    
    /**
     * Crea una nueva instancia del fragmento con datos de pago opcional
     * @param paymentRequestId ID de la solicitud de pago (opcional)
     * @param paymentMethod Método de pago utilizado (opcional)
     * @return Una nueva instancia del fragmento
     */
    public static SuccessFragment newInstance(String paymentRequestId, String paymentMethod) {
        SuccessFragment fragment = new SuccessFragment();
        Bundle args = new Bundle();
        args.putString("payment_request_id", paymentRequestId);
        args.putString("payment_method", paymentMethod);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paymentRequestId = getArguments().getString("payment_request_id");
            paymentMethod = getArguments().getString("payment_method");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_success, container, false);
        
        // Inicializar vistas
        textMessage = view.findViewById(R.id.textView);
        Button button = view.findViewById(R.id.button);
        
        // Mini-ticket visual
        ViewGroup ticketLayout = new android.widget.LinearLayout(requireContext());
        ((android.widget.LinearLayout) ticketLayout).setOrientation(android.widget.LinearLayout.VERTICAL);
        ticketLayout.setPadding(32, 32, 32, 32);
        ticketLayout.setBackgroundResource(android.R.color.white);
        ticketLayout.setElevation(8f);
        ticketLayout.setClickable(true);
        ticketLayout.setFocusable(true);
        ticketLayout.setForeground(requireContext().getDrawable(android.R.drawable.list_selector_background));
        android.widget.TextView tvTitulo = new android.widget.TextView(requireContext());
        tvTitulo.setText("Ticket de compra");
        tvTitulo.setTextSize(18);
        tvTitulo.setTextColor(android.graphics.Color.BLACK);
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        ticketLayout.addView(tvTitulo);
        // Fecha
        final String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
        android.widget.TextView tvFecha = new android.widget.TextView(requireContext());
        tvFecha.setText("Fecha: " + fecha);
        tvFecha.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvFecha);
        // Número de pedido simulado
        final int nroPedido = (int) (Math.random() * 90000 + 10000);
        android.widget.TextView tvNro = new android.widget.TextView(requireContext());
        tvNro.setText("N° Pedido: " + nroPedido);
        tvNro.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvNro);
        // Método de pago
        android.widget.TextView tvPago = new android.widget.TextView(requireContext());
        final String metodo;
        if (paymentMethod != null) {
            String paymentMethodLower = paymentMethod.toLowerCase();
            if (paymentMethodLower.contains("mercado") || paymentMethodLower.equals("mercadopago")) {
                metodo = "Mercado Pago";
            } else if (paymentMethodLower.contains("paypal") || paymentMethodLower.equals("pay pal")) {
                metodo = "PayPal";
            } else if (paymentMethodLower.contains("credit") || paymentMethodLower.contains("tarjeta") ||
                       paymentMethodLower.contains("card")) {
                metodo = "Tarjeta de crédito";
            } else {
                metodo = paymentMethod; // Usar el valor original si no coincide con ninguno de los casos anteriores
            }
        } else {
            metodo = "Efectivo"; // Valor por defecto si no se especifica método de pago
        }
        tvPago.setText("Pago: " + metodo);
        tvPago.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvPago);

        // Obtener el costo de envío guardado desde DatosEntregaFragment
        final String envio = obtenerCostoEnvio();

        // Obtener el subtotal directamente del carrito que se guardó en la sesión
        final String subtotalCarrito = obtenerSubtotal(requireContext());
        final double subtotalDouble = Double.parseDouble(subtotalCarrito);
        final double envioDouble = Double.parseDouble(envio);
        final double totalDouble = subtotalDouble + envioDouble;
        final String totalStr = String.format("%.2f", totalDouble);

        // TextView para el subtotal - Desde el carrito
        android.widget.TextView tvSubtotal = new android.widget.TextView(requireContext());
        tvSubtotal.setText("Subtotal: $ " + subtotalCarrito);
        tvSubtotal.setTextSize(15);
        tvSubtotal.setTextColor(android.graphics.Color.DKGRAY);
        tvSubtotal.setPadding(0, 8, 0, 4);
        ticketLayout.addView(tvSubtotal);

        // TextView para el envío
        android.widget.TextView tvEnvio = new android.widget.TextView(requireContext());
        tvEnvio.setText("Envío: $ " + envio);
        tvEnvio.setTextSize(15);
        tvEnvio.setTextColor(android.graphics.Color.DKGRAY);
        tvEnvio.setPadding(0, 4, 0, 4);
        ticketLayout.addView(tvEnvio);

        // Crear TextView para el total (sumando subtotal del carrito + envío)
        android.widget.TextView tvTotal = new android.widget.TextView(requireContext());
        tvTotal.setText("Total: $ " + totalStr);
        tvTotal.setTextSize(16);
        tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotal.setTextColor(android.graphics.Color.BLACK);
        tvTotal.setPadding(0, 8, 0, 8);
        ticketLayout.addView(tvTotal);

        // Separador visual
        android.view.View sep = new android.view.View(requireContext());
        sep.setBackgroundColor(android.graphics.Color.LTGRAY);
        sep.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2));
        ticketLayout.addView(sep);
        // Mensaje de éxito
        android.widget.TextView tvGracias = new android.widget.TextView(requireContext());
        tvGracias.setText("¡Gracias por tu compra!");
        tvGracias.setTextColor(android.graphics.Color.parseColor("#388E3C"));
        tvGracias.setTextSize(16);
        tvGracias.setPadding(0, 16, 0, 0);
        ticketLayout.addView(tvGracias);
        // Agregar el ticket al contenedor del layout XML
        FrameLayout ticketContainer = view.findViewById(R.id.ticket_container);
        ticketContainer.removeAllViews();
        ticketContainer.addView(ticketLayout);
        // Personalizar mensaje según el método de pago
        if (paymentMethod != null && paymentMethod.equalsIgnoreCase("mercadopago")) {
            textMessage.setText("¡Compra Finalizada con Éxito!\n\nTu pago con MercadoPago ha sido procesado correctamente.");
        } else {
            textMessage.setText("¡Compra Finalizada con Éxito!");
        }
        // Limpiar carrito en memoria (opcional)
        SessionManager sessionManager = new SessionManager(requireContext());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Llamar a MainActivity para limpiar la pila y mostrar HomeFragment correctamente
                    if (getActivity() instanceof com.example.food_front.MainActivity) {
                        ((com.example.food_front.MainActivity) getActivity()).mostrarHomeLimpiandoBackStack();
                    } else {
                        // Fallback por si acaso
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container_view, new HomeFragment());
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        fragmentTransaction.commit();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error al navegar al HomeFragment: " + e.getMessage());
                    Toast.makeText(requireContext(), "Error al volver a inicio. Intente nuevamente.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Obtener productos reales del último pedido
        String token = sessionManager.getToken();
        DashboardHelper.getUltimoPedido(requireContext(), token, new DashboardHelper.DashboardCallback() {
            @Override
            public void onSuccess(org.json.JSONArray pedidos) {
                if (pedidos.length() > 0) {
                    try {
                        org.json.JSONObject ultimoPedido = pedidos.getJSONObject(0); // El más reciente
                        StringBuilder productosBuilder = new StringBuilder();
                        double subtotalDouble = 0.0;

                        if (ultimoPedido.has("detalles")) {
                            org.json.JSONArray detalles = ultimoPedido.getJSONArray("detalles");
                            for (int i = 0; i < detalles.length(); i++) {
                                org.json.JSONObject detalle = detalles.getJSONObject(i);
                                String nombre = detalle.optString("nombre_producto", "Producto");
                                int cantidad = detalle.optInt("cantidad_productos", 1);
                                double precio = detalle.optDouble("precio_unitario", 0);

                                productosBuilder.append("- ").append(nombre).append(" x").append(cantidad).append("\n");

                                // Calcular subtotal sumando los productos
                                subtotalDouble += precio * cantidad;
                            }
                        }

                        final String productos = productosBuilder.toString().trim();
                        final String subtotal = String.format("%.2f", subtotalDouble);

                        // Calcular total (subtotal + envío)
                        double totalDouble = subtotalDouble + Double.parseDouble(envio);
                        final String total = String.format("%.2f", totalDouble);

                        // Actualizar el subtotal y total en el ticket resumen
                        tvSubtotal.setText("Subtotal: $ " + subtotal);
                        tvTotal.setText("Total: $ " + total);

                        // Guardar estos valores para usarlos en el ticket ampliado
                        guardarSubtotalYEnvio(requireContext(), subtotal, envio);

                        // Hacer el ticket ampliable al click con productos reales
                        ticketLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                                        fecha,
                                        String.valueOf(nroPedido),
                                        metodo,
                                        productos,
                                        subtotal,
                                        envio,
                                        total
                                );
                                dialog.show(getParentFragmentManager(), "TicketDetailDialog");
                            }
                        });
                    } catch (Exception e) {
                        // Fallback a productos hardcodeados si hay error
                        setTicketClickDefault(ticketLayout, fecha, nroPedido, metodo, envio);
                    }
                } else {
                    // Fallback a productos hardcodeados si no hay pedidos
                    setTicketClickDefault(ticketLayout, fecha, nroPedido, metodo, envio);
                }
            }
            @Override
            public void onError(String error) {
                // Fallback a productos hardcodeados si hay error
                setTicketClickDefault(ticketLayout, fecha, nroPedido, metodo, envio);
            }
        });
        return view;
    }

    private void setTicketClickDefault(View ticketLayout, String fecha, int nroPedido, String metodo, String envio) {
        final String productos = "- Hamburguesa x2\n- Papas Fritas x1\n- Bebida x1";

        // Calcular un subtotal ficticio para los productos hardcodeados
        double subtotalDouble = 3000.0; // Valor arbitrario para productos hardcodeados
        final String subtotal = String.format("%.2f", subtotalDouble);

        // Calcular total (subtotal + envío)
        double totalDouble = subtotalDouble + Double.parseDouble(envio);
        final String total = String.format("%.2f", totalDouble);

        ticketLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                        fecha,
                        String.valueOf(nroPedido),
                        metodo,
                        productos,
                        subtotal,
                        envio,
                        total
                );
                dialog.show(getParentFragmentManager(), "TicketDetailDialog");
            }
        });
    }

    // --- UTILIDADES PARA SUBTOTAL Y ENVIO ---
    public static void guardarSubtotalYEnvio(android.content.Context ctx, String subtotal, String envio) {
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putString("subtotal", subtotal).putString("envio", envio).apply();
    }
    public static String obtenerSubtotal(android.content.Context ctx) {
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("subtotal", "0.00");
    }
    public static String obtenerEnvio(android.content.Context ctx) {
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("envio", "0.00");
    }

    // Método para obtener el costo de envío
    private String obtenerCostoEnvio() {
        String envio;
        try {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
            envio = prefs.getString("envio", "0.00");
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener costo de envío: " + e.getMessage());
            envio = "0.00";
        }
        return envio;
    }
}
