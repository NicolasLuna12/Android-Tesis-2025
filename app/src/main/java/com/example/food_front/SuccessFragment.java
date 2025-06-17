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
     * @param paymentMethod M�todo de pago utilizado (opcional)
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
        // N�mero de pedido simulado
        final int nroPedido = (int) (Math.random() * 90000 + 10000);
        android.widget.TextView tvNro = new android.widget.TextView(requireContext());
        tvNro.setText("N Pedido: " + nroPedido);
        tvNro.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvNro);
        // M�todo de pago
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
                metodo = "Tarjeta de cr�dito";
            } else {
                metodo = paymentMethod; // Usar el valor original si no coincide con ninguno de los casos anteriores
            }
        } else {
            metodo = "Efectivo"; // Valor por defecto si no se especifica m�todo de pago
        }
        tvPago.setText("Pago: " + metodo);
        tvPago.setTextColor(android.graphics.Color.DKGRAY);
        ticketLayout.addView(tvPago);

        // Obtener el costo de env�o guardado desde DatosEntregaFragment
        final String envio = obtenerCostoEnvio();

        // Obtener el subtotal directamente del carrito que se guard� en la sesi�n
        final String subtotalCarrito = obtenerSubtotal(requireContext());
        final double subtotalDouble = Double.parseDouble(subtotalCarrito);
        final double envioDouble = Double.parseDouble(envio);
        final double totalDouble = subtotalDouble + envioDouble;
        final String totalStr = formatearValorMonetario(String.valueOf(totalDouble));
        
        // TextView para el subtotal - Desde el carrito
        android.widget.TextView tvSubtotal = new android.widget.TextView(requireContext());
        tvSubtotal.setId(android.view.View.generateViewId()); // Asignar un ID �nico para poder referenciarlo despu�s
        tvSubtotal.setText("Subtotal: $ " + subtotalCarrito);
        tvSubtotal.setTextSize(15);
        tvSubtotal.setTextColor(android.graphics.Color.DKGRAY);
        tvSubtotal.setPadding(0, 8, 0, 4);
        tvSubtotal.setTag("subtotal_view"); // Tag para identificar esta vista espec�ficamente
        ticketLayout.addView(tvSubtotal);

        // TextView para el env�o
        android.widget.TextView tvEnvio = new android.widget.TextView(requireContext());
        tvEnvio.setText("Env�o: $ " + envio);
        tvEnvio.setTextSize(15);
        tvEnvio.setTextColor(android.graphics.Color.DKGRAY);
        tvEnvio.setPadding(0, 4, 0, 4);
        tvEnvio.setTag("envio_view"); // Tag para identificar esta vista espec�ficamente
        ticketLayout.addView(tvEnvio);        
        
        // Crear TextView para el total (sumando subtotal del carrito + env�o)
        android.widget.TextView tvTotal = new android.widget.TextView(requireContext());
        tvTotal.setId(android.view.View.generateViewId()); // Asignar un ID �nico para poder referenciarlo despu�s
        tvTotal.setText("Total: $ " + totalStr);
        tvTotal.setTextSize(16);
        tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTotal.setTextColor(android.graphics.Color.BLACK);
        tvTotal.setPadding(0, 8, 0, 8);
        tvTotal.setTag("total_view"); // Tag para identificar esta vista espec�ficamente
        ticketLayout.addView(tvTotal);

        // Separador visual
        android.view.View sep = new android.view.View(requireContext());
        sep.setBackgroundColor(android.graphics.Color.LTGRAY);
        sep.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 2));
        ticketLayout.addView(sep);
        // Mensaje de �xito
        android.widget.TextView tvGracias = new android.widget.TextView(requireContext());
        tvGracias.setText("Gracias por tu compra!");
        tvGracias.setTextColor(android.graphics.Color.parseColor("#388E3C"));
        tvGracias.setTextSize(16);
        tvGracias.setPadding(0, 16, 0, 0);
        ticketLayout.addView(tvGracias);
        // Agregar el ticket al contenedor del layout XML
        FrameLayout ticketContainer = view.findViewById(R.id.ticket_container);
        ticketContainer.removeAllViews();
        ticketContainer.addView(ticketLayout);
        // Personalizar mensaje seg�n el m�todo de pago
        if (paymentMethod != null && paymentMethod.equalsIgnoreCase("mercadopago")) {
            textMessage.setText("Compra Finalizada con �xito!\n\nTu pago con MercadoPago ha sido procesado correctamente.");
        } else {
            textMessage.setText("Compra Finalizada con �xito!");
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
        
        // Guardar los valores actuales antes de buscar los productos reales
        guardarSubtotalYEnvio(requireContext(), subtotalCarrito, envio);

        // Obtener productos reales del �ltimo pedido
        String token = sessionManager.getToken();
        Log.d(TAG, "Obteniendo �ltimo pedido con token: " + (token != null ? "disponible" : "no disponible"));
        DashboardHelper.getUltimoPedido(requireContext(), token, new DashboardHelper.DashboardCallback() {
            @Override
            public void onSuccess(org.json.JSONArray pedidos) {
                if (pedidos.length() > 0) {
                    try {
                        org.json.JSONObject ultimoPedido = pedidos.getJSONObject(0); // El m�s reciente
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
                        
                        // Si no hay productos, usamos el subtotal guardado anteriormente
                        final String subtotal;
                        if (subtotalDouble <= 0) {
                            subtotal = obtenerSubtotal(requireContext());
                            Log.d(TAG, "Usando subtotal guardado porque no hay productos en el �ltimo pedido: " + subtotal);
                        } else {
                            subtotal = formatearValorMonetario(String.valueOf(subtotalDouble));
                            Log.d(TAG, "Usando subtotal calculado de los productos: " + subtotal);
                        }
                        
                        // Calcular total (subtotal + env�o)
                        double totalDouble = Double.parseDouble(subtotal) + Double.parseDouble(envio);
                        final String total = formatearValorMonetario(String.valueOf(totalDouble));
                        
                        // Guardar estos valores para usarlos en el ticket ampliado
                        guardarSubtotalYEnvio(requireContext(), subtotal, envio);
                        
                        // Actualizar el subtotal y total en el ticket resumen usando nuestro m�todo dedicado
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Solo fijamos los valores una vez para evitar duplicaci�n
                                fijarValoresTicket((ViewGroup)ticketLayout, subtotal, envio, total);
                                
                                // En lugar de un segundo llamado, solo invalidamos la vista
                                ticketLayout.invalidate();
                                ticketLayout.requestLayout();
                            }
                        });
                        
                        // Hacer el ticket ampliable al click con productos reales
                        final String productosFinales = productos; // Guardamos en variable final para el onClick
                        ticketLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Obtener valores actuales directamente de SharedPreferences
                                // para mayor consistencia
                                String subtotalActual = obtenerSubtotal(requireContext());
                                String envioActual = envio; // Usar el env�o pasado como par�metro
                                
                                // Asegurarnos que tengan formato correcto
                                subtotalActual = formatearValorMonetario(subtotalActual);
                                envioActual = formatearValorMonetario(envioActual);
                                
                                // Calcular total actual
                                double subtotalDouble = Double.parseDouble(subtotalActual);
                                double envioDouble = Double.parseDouble(envioActual);
                                String totalActual = formatearValorMonetario(String.valueOf(subtotalDouble + envioDouble));
                                
                                Log.d(TAG, "onClick ticket (API): Usando valores - Subtotal: " + subtotalActual + ", Env�o: " + envioActual + ", Total: " + totalActual);
                                
                                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                                        fecha,
                                        String.valueOf(nroPedido),
                                        metodo,
                                        productosFinales,
                                        subtotalActual,
                                        envio,
                                        totalActual
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
        // Intentar obtener un carrito existente de la sesi�n
        String productos = "";
        double subtotalDouble = 0.0;
        boolean hayProductosEnCarrito = false;
        
        try {
            SessionManager sessionManager = new SessionManager(requireContext());
            String carritoJson = sessionManager.getCarrito();
            
            Log.d(TAG, "Contenido del carrito en JSON: " + carritoJson);
            
            if (carritoJson != null && !carritoJson.isEmpty() && !carritoJson.equals("[]")) {
                org.json.JSONArray carrito = new org.json.JSONArray(carritoJson);
                StringBuilder productosBuilder = new StringBuilder();
                
                for (int i = 0; i < carrito.length(); i++) {
                    org.json.JSONObject producto = carrito.getJSONObject(i);
                    String nombre = producto.optString("name", "Producto");
                    int cantidad = producto.optInt("quantity", 1);
                    double precio = producto.optDouble("price", 0);
                    
                    productosBuilder.append("- ").append(nombre).append(" x").append(cantidad).append("\n");
                    subtotalDouble += precio * cantidad;
                    hayProductosEnCarrito = true;
                }
                
                if (hayProductosEnCarrito) {
                    productos = productosBuilder.toString().trim();
                    Log.d(TAG, "Usando productos del carrito para el ticket: " + productos);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener productos del carrito: " + e.getMessage());
        }
        
        // Si no hay productos en el carrito, usar subtotal guardado o uno por defecto
        final String subtotal;
        if (!hayProductosEnCarrito || subtotalDouble <= 0) {
            subtotal = obtenerSubtotal(requireContext());
            productos = "- Hamburguesa x2\n- Papas Fritas x1\n- Bebida x1";
            Log.d(TAG, "Usando productos por defecto y subtotal guardado: " + subtotal);
        } else {
            subtotal = formatearValorMonetario(String.valueOf(subtotalDouble));
            Log.d(TAG, "Usando subtotal calculado del carrito: " + subtotal);
        }

        // Preservar los productos finales para el di�logo de detalle
        final String productosFinales = productos;
        
        // Calcular total (subtotal + env�o)
        double envioDouble = Double.parseDouble(formatearValorMonetario(envio));
        double totalDouble = Double.parseDouble(subtotal) + envioDouble;
        final String total = formatearValorMonetario(String.valueOf(totalDouble));
        
        // Guardar estos valores en SharedPreferences inmediatamente
        guardarSubtotalYEnvio(requireContext(), subtotal, envio);
        
        // Actualizar el subtotal y total en el ticket usando nuestro m�todo dedicado (una sola vez)
        fijarValoresTicket((ViewGroup)ticketLayout, subtotal, envio, total);
        
        ticketLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener valores actuales directamente de SharedPreferences
                String subtotalActual = obtenerSubtotal(requireContext());
                String envioActual = obtenerEnvio(requireContext());
                
                // Calcular total actual
                double subtotalDouble = Double.parseDouble(subtotalActual);
                double envioDouble = Double.parseDouble(envioActual);
                String totalActual = formatearValorMonetario(String.valueOf(subtotalDouble + envioDouble));
                
                Log.d(TAG, "onClick ticket (default): Usando valores - Subtotal: " + subtotalActual + ", Env�o: " + envioActual + ", Total: " + totalActual);
                
                // Pasar los valores actuales al di�logo
                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                        fecha,
                        String.valueOf(nroPedido),
                        metodo,
                        productosFinales, // Usar los productos obtenidos del carrito
                        subtotalActual,  // Usar el valor actualizado
                        envioActual,    // Usar el valor actualizado
                        totalActual      // Usar el valor actualizado
                );
                dialog.show(getParentFragmentManager(), "TicketDetailDialog");
            }
        });
    }

    // --- UTILIDADES PARA SUBTOTAL Y ENVIO ---
    public static void guardarSubtotalYEnvio(android.content.Context ctx, String subtotal, String envio) {
        if (ctx == null) {
            android.util.Log.e(TAG, "Context nulo al intentar guardar subtotal y env�o");
            return;
        }
        
        // Asegurar que los valores tengan 2 decimales
        String subtotalFormatted = formatearValorMonetario(subtotal);
        String envioFormatted = formatearValorMonetario(envio);
        
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("subtotal", subtotalFormatted);
        editor.putString("envio", envioFormatted);
        editor.commit(); // Usando commit en lugar de apply para asegurar escritura inmediata
        
        android.util.Log.d(TAG, "Guardando en SharedPreferences - Subtotal: " + subtotalFormatted + ", Env�o: " + envioFormatted);
    }
    
    public static String obtenerSubtotal(android.content.Context ctx) {
        if (ctx == null) {
            android.util.Log.e(TAG, "Context nulo al intentar obtener subtotal");
            return "0.00";
        }
        
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        String subtotal = prefs.getString("subtotal", "0.00");
        
        // Asegurar formato con 2 decimales
        subtotal = formatearValorMonetario(subtotal);
        android.util.Log.d(TAG, "Obteniendo subtotal desde SharedPreferences: " + subtotal);
        return subtotal;
    }
    
    public static String obtenerEnvio(android.content.Context ctx) {
        if (ctx == null) {
            android.util.Log.e(TAG, "Context nulo al intentar obtener env�o");
            return "0.00";
        }
        
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        String envio = prefs.getString("envio", "0.00");
        
        // Asegurar formato con 2 decimales
        envio = formatearValorMonetario(envio);
        android.util.Log.d(TAG, "Obteniendo env�o desde SharedPreferences: " + envio);
        return envio;
    }

    // M�todo para obtener el costo de env�o
    private String obtenerCostoEnvio() {
        String envio;
        try {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
            envio = prefs.getString("envio", "0.00");
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener costo de env�o: " + e.getMessage());
            envio = "0.00";
        }
        return formatearValorMonetario(envio);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Al resumir, solo registramos que estamos de vuelta pero NO actualizamos
        // los valores para evitar la duplicaci�n
        Log.d(TAG, "onResume: Fragmento resumido");
    }
    
    /**
     * M�todo para actualizar los valores en el ticket sin duplicaci�n
     */
    private void fijarValoresTicket(ViewGroup ticketLayout, String subtotal, String envio, String total) {
        Log.d(TAG, "M�todo fijarValoresTicket llamado con subtotal=" + subtotal + ", envio=" + envio + ", total=" + total);
        
        // Asegurar que los valores tengan formato correcto
        String subtotalFormatted = formatearValorMonetario(subtotal);
        String envioFormatted = formatearValorMonetario(envio);
        String totalFormatted = formatearValorMonetario(total);
        
        Log.d(TAG, "Valores formateados: subtotal=" + subtotalFormatted + ", envio=" + envioFormatted + ", total=" + totalFormatted);
        
        // Buscar los TextViews por sus tags espec�ficos
        TextView tvSubtotal = null;
        TextView tvTotal = null;
        
        for (int i = 0; i < ticketLayout.getChildCount(); i++) {
            View child = ticketLayout.getChildAt(i);
            if (child instanceof TextView) {
                Object tag = child.getTag();
                if (tag != null) {
                    if ("subtotal_view".equals(tag.toString())) {
                        tvSubtotal = (TextView) child;
                    } else if ("total_view".equals(tag.toString())) {
                        tvTotal = (TextView) child;
                    }
                }
            }
        }
        
        // Si los TextViews no se encontraron por tag, buscarlos por el texto
        if (tvSubtotal == null || tvTotal == null) {
            for (int i = 0; i < ticketLayout.getChildCount(); i++) {
                View child = ticketLayout.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    String text = tv.getText().toString();
                    if (text != null && text.contains("Subtotal")) {
                        tvSubtotal = tv;
                        tvSubtotal.setTag("subtotal_view");
                    } else if (text != null && text.contains("Total") && !text.contains("Subtotal") && !text.contains("Env�o")) {
                        tvTotal = tv;
                        tvTotal.setTag("total_view");
                    }
                }
            }
        }
        
        // Actualizar los valores solo si encontramos las vistas
        if (tvSubtotal != null) {
            tvSubtotal.setText("Subtotal: $ " + subtotalFormatted);
            Log.d(TAG, "Subtotal actualizado a: " + subtotalFormatted);
        } else {
            Log.w(TAG, "No se encontr� TextView para subtotal");
        }
        
        if (tvTotal != null) {
            tvTotal.setText("Total: $ " + totalFormatted);
            Log.d(TAG, "Total actualizado a: " + totalFormatted);
        } else {
            Log.w(TAG, "No se encontr� TextView para total");
        }
        
        // Guardar valores en SharedPreferences
        guardarSubtotalYEnvio(ticketLayout.getContext(), subtotalFormatted, envioFormatted);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Asegurar que los valores se guarden antes de que el fragmento se destruya
        if (getContext() != null) {
            String subtotal = obtenerSubtotal(requireContext());
            String envio = obtenerEnvio(requireContext());
            guardarSubtotalYEnvio(requireContext(), subtotal, envio);
        }
    }
    
    /**
     * M�todo auxiliar para asegurar que todos los valores monetarios se formateen con 2 decimales
     */
    private static String formatearValorMonetario(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "0.00";
        }
        
        try {
            // Eliminar cualquier car�cter no num�rico excepto el punto decimal
            String valorLimpio = valor.replaceAll("[^\\d.]", "");
            double valorDouble = Double.parseDouble(valorLimpio);
            return String.format(java.util.Locale.US, "%.2f", valorDouble);
        } catch (NumberFormatException e) {
            android.util.Log.e(TAG, "Error al formatear valor monetario: " + valor, e);
            return "0.00";
        }
    }
}
