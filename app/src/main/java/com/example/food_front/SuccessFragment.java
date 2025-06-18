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
        Button btnVerTicket = view.findViewById(R.id.btnVerTicket);
        FrameLayout ticketContainer = view.findViewById(R.id.ticket_container);

        // Mini-ticket visual
        ViewGroup ticketLayout = new android.widget.LinearLayout(requireContext());
        ((android.widget.LinearLayout) ticketLayout).setOrientation(android.widget.LinearLayout.VERTICAL);
        ticketLayout.setPadding(32, 32, 32, 32);
        
        // Crear un fondo con bordes redondeados para el ticket
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setCornerRadius(16f);
        shape.setColor(android.graphics.Color.WHITE);
        shape.setStroke(1, android.graphics.Color.LTGRAY);
        ticketLayout.setBackground(shape);
        
        ticketLayout.setElevation(8f);
        ticketLayout.setClickable(true);
        ticketLayout.setFocusable(true);
        ticketLayout.setForeground(requireContext().getDrawable(android.R.drawable.list_selector_background));
        
        // Imagen de confirmación en la parte superior del ticket
        android.widget.ImageView checkImage = new android.widget.ImageView(requireContext());
        checkImage.setImageResource(android.R.drawable.ic_menu_send);
        checkImage.setColorFilter(android.graphics.Color.parseColor("#388E3C"));
        android.widget.LinearLayout.LayoutParams imageParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.gravity = android.view.Gravity.CENTER;
        imageParams.bottomMargin = 16;
        checkImage.setLayoutParams(imageParams);
        ticketLayout.addView(checkImage);
        
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

        // Agregar separador antes de lista de productos
        android.view.View separadorSuperior = new android.view.View(requireContext());
        separadorSuperior.setBackgroundColor(android.graphics.Color.LTGRAY);
        separadorSuperior.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
        ticketLayout.addView(separadorSuperior);

        // Título para la sección de productos
        android.widget.TextView tvProductosTitle = new android.widget.TextView(requireContext());
        tvProductosTitle.setText("Productos:");
        tvProductosTitle.setTextSize(15);
        tvProductosTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        tvProductosTitle.setPadding(0, 12, 0, 8);
        ticketLayout.addView(tvProductosTitle);        // Mostrar los productos del carrito
        mostrarProductosEnTicket(ticketLayout);
          // Obtener el costo de envío guardado desde DatosEntregaFragment
        final String envio = obtenerEnvio(requireContext());

        // Obtener el subtotal directamente de las preferencias
        final String subtotalCarrito = obtenerSubtotal(requireContext());
        final double subtotalDouble = Double.parseDouble(subtotalCarrito);
        final double envioDouble = Double.parseDouble(envio);
        final double totalDouble = subtotalDouble + envioDouble;
        final String totalStr = formatearValorMonetarioStatic(String.valueOf(totalDouble));
        
        // Usar nuestro método centralizado para fijar los valores de subtotal, envío y total
        fijarValoresTicket(ticketLayout, subtotalCarrito, envio, totalStr);

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
        ticketContainer.removeAllViews();
        ticketContainer.addView(ticketLayout);
        
        // Personalizar mensaje según el método de pago
        if (paymentMethod != null && paymentMethod.equalsIgnoreCase("mercadopago")) {
            textMessage.setText("Compra Finalizada con exito!\n\nTu pago con MercadoPago ha sido procesado correctamente.");
        } else {
            textMessage.setText("Compra Finalizada con exito!");
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

        btnVerTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TicketDetailDialogFragment dialog = TicketDetailDialogFragment.newInstance(
                        new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()),
                        String.valueOf((int) (Math.random() * 90000 + 10000)),
                        paymentMethod != null ? paymentMethod : "Efectivo",
                        "-", // productos, el fragmento los obtiene solo
                        obtenerSubtotal(requireContext()),
                        obtenerEnvio(requireContext()),
                        formatearValorMonetarioStatic(String.valueOf(Double.parseDouble(obtenerSubtotal(requireContext())) + Double.parseDouble(obtenerEnvio(requireContext()))) )
                );
                dialog.show(requireActivity().getSupportFragmentManager(), "TicketDetailDialog");
            }
        });
        return view;
    }
      private void setTicketClickDefault(View ticketLayout, String fecha, int nroPedido, String metodo, String envio) {
        // Obtener productos del carrito desde SharedPreferences (guardados por CartFragment)
        String productos = "";
        double subtotalDouble = 0.0;
        boolean hayProductosEnCarrito = false;
        
        try {
            // Obtener el JSON de productos guardado en SharedPreferences
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
            String productosJson = prefs.getString("productos_carrito", "");
            
            Log.d(TAG, "Contenido de productos en SharedPreferences: " + productosJson);
            
            if (productosJson != null && !productosJson.isEmpty()) {
                org.json.JSONArray productosArray = new org.json.JSONArray(productosJson);
                StringBuilder productosBuilder = new StringBuilder();
                
                for (int i = 0; i < productosArray.length(); i++) {
                    org.json.JSONObject producto = productosArray.getJSONObject(i);
                    String nombre = producto.optString("nombre", "Producto");
                    int cantidad = producto.optInt("cantidad", 1);
                    double precio = producto.optDouble("precio", 0);
                    
                    productosBuilder.append("- ").append(nombre).append(" x").append(cantidad).append("\n");
                    subtotalDouble += precio * cantidad;
                    hayProductosEnCarrito = true;
                }
                
                if (hayProductosEnCarrito) {
                    productos = productosBuilder.toString().trim();
                    Log.d(TAG, "Usando productos de SharedPreferences para el ticket: " + productos);
                }
            }
            
            // Si no hay datos en SharedPreferences, intentar obtenerlos del SessionManager como respaldo
            if (!hayProductosEnCarrito) {
                SessionManager sessionManager = new SessionManager(requireContext());
                String carritoJson = sessionManager.getCarrito();
                
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
                        Log.d(TAG, "Usando productos de SessionManager para el ticket: " + productos);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener productos para el ticket: " + e.getMessage());
        }
        
        // Si no hay productos, mostrar mensaje sin productos hardcodeados
        final String subtotal;
        if (!hayProductosEnCarrito || subtotalDouble <= 0) {
            subtotal = obtenerSubtotal(requireContext());
            productos = "No hay detalles de productos disponibles";
            Log.d(TAG, "No se encontraron productos, usando subtotal guardado: " + subtotal);
        } else {
            subtotal = formatearValorMonetario(String.valueOf(subtotalDouble));
            Log.d(TAG, "Usando subtotal calculado de los productos: " + subtotal);
        }

        // Preservar los productos finales para el diálogo de detalle
        final String productosFinales = productos;
          // Calcular total (subtotal + env�o)
        double envioDouble = Double.parseDouble(formatearValorMonetarioStatic(envio));
        double totalDouble = Double.parseDouble(subtotal) + envioDouble;
        final String total = formatearValorMonetarioStatic(String.valueOf(totalDouble));
        
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
        String subtotalFormatted = formatearValorMonetarioStatic(subtotal);
        String envioFormatted = formatearValorMonetarioStatic(envio);
        
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("subtotal", subtotalFormatted);
        editor.putString("envio", envioFormatted);
        editor.commit(); // Usando commit en lugar de apply para asegurar escritura inmediata
        
        android.util.Log.d(TAG, "Guardando en SharedPreferences - Subtotal: " + subtotalFormatted + ", Env�o: " + envioFormatted);    }
    
    public static String obtenerSubtotal(android.content.Context ctx) {
        if (ctx == null) {
            android.util.Log.e(TAG, "Context nulo al intentar obtener subtotal");
            return "0.00";
        }
        
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        String subtotal = prefs.getString("subtotal", "0.00");
        
        // Asegurar formato con 2 decimales
        subtotal = formatearValorMonetarioStatic(subtotal);
        android.util.Log.d(TAG, "Obteniendo subtotal desde SharedPreferences: " + subtotal);
        return subtotal;    }
    
    public static String obtenerEnvio(android.content.Context ctx) {
        if (ctx == null) {
            android.util.Log.e(TAG, "Context nulo al intentar obtener env�o");
            return "0.00";
        }
        
        android.content.SharedPreferences prefs = ctx.getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        String envio = prefs.getString("envio", "0.00");
        
        // Asegurar formato con 2 decimales
        envio = formatearValorMonetarioStatic(envio);
        android.util.Log.d(TAG, "Obteniendo env�o desde SharedPreferences: " + envio);
        return envio;
    }

    // M�todo para obtener el costo de env�o
    private String obtenerCostoEnvio() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("envio", "0");    }    
    
    // El método obtenerSubtotal ya está definido como estático
      /**
     * Muestra los productos del carrito en el ticket de forma resumida
     * @param ticketLayout Layout donde se mostrarán los productos
     */
    private void mostrarProductosEnTicket(ViewGroup ticketLayout) {
        try {
            // Obtener el JSON de productos guardado en SharedPreferences
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ticket_prefs", android.content.Context.MODE_PRIVATE);
            String productosJson = prefs.getString("productos_carrito", "");
            
            // Variable para rastrear si encontramos productos
            boolean productosEncontrados = false;
            org.json.JSONArray jsonArray = null;
            
            // Intentar leer productos de SharedPreferences primero
            if (productosJson != null && !productosJson.isEmpty()) {
                jsonArray = new org.json.JSONArray(productosJson);
                if (jsonArray.length() > 0) {
                    productosEncontrados = true;
                    Log.d(TAG, "Productos encontrados en SharedPreferences: " + jsonArray.length());
                }
            }
            
            // Si no se encontraron productos en SharedPreferences, intentar con SessionManager
            if (!productosEncontrados) {
                SessionManager sessionManager = new SessionManager(requireContext());
                String carritoJson = sessionManager.getCarrito();
                
                if (carritoJson != null && !carritoJson.isEmpty() && !carritoJson.equals("[]")) {
                    org.json.JSONArray carrito = new org.json.JSONArray(carritoJson);
                    if (carrito.length() > 0) {
                        // Convertir el formato de SessionManager al formato usado en SharedPreferences
                        jsonArray = new org.json.JSONArray();
                        for (int i = 0; i < carrito.length(); i++) {
                            org.json.JSONObject producto = carrito.getJSONObject(i);
                            
                            org.json.JSONObject nuevoProducto = new org.json.JSONObject();
                            nuevoProducto.put("nombre", producto.optString("name", "Producto"));
                            nuevoProducto.put("cantidad", producto.optInt("quantity", 1));
                            nuevoProducto.put("precio", producto.optDouble("price", 0));
                            nuevoProducto.put("imagen", producto.optString("image", ""));
                            
                            jsonArray.put(nuevoProducto);
                            productosEncontrados = true;
                        }
                        
                        Log.d(TAG, "Productos encontrados en SessionManager: " + jsonArray.length());
                        
                        // Guardar estos productos en SharedPreferences para futuros usos
                        prefs.edit().putString("productos_carrito", jsonArray.toString()).apply();
                    }
                }
            }
            
            // Si no se encontraron productos en ninguna fuente
            if (!productosEncontrados || jsonArray == null || jsonArray.length() == 0) {
                android.widget.TextView tvNoProductos = new android.widget.TextView(requireContext());
                tvNoProductos.setText("No hay productos disponibles para mostrar");
                tvNoProductos.setTextColor(android.graphics.Color.DKGRAY);
                tvNoProductos.setTextSize(14);
                tvNoProductos.setPadding(0, 8, 0, 8);
                ticketLayout.addView(tvNoProductos);
                return;
            }
            
            // Mostrar resumen de productos
            android.widget.TextView tvResumen = new android.widget.TextView(requireContext());
            String resumenText = jsonArray.length() == 1 ? 
                "1 producto en el carrito" : 
                jsonArray.length() + " productos en el carrito";
            tvResumen.setText(resumenText);
            tvResumen.setTextSize(14);
            tvResumen.setTypeface(null, android.graphics.Typeface.BOLD);
            tvResumen.setTextColor(android.graphics.Color.DKGRAY);
            tvResumen.setPadding(0, 6, 0, 10);
            ticketLayout.addView(tvResumen);
            
            // Mostrar solo los primeros 3 productos (o menos si hay menos)
            int maxProductosAMostrar = Math.min(jsonArray.length(), 3);
            double total = 0.0;
            
            for (int i = 0; i < maxProductosAMostrar; i++) {
                org.json.JSONObject producto = jsonArray.getJSONObject(i);
                
                // Crear un layout horizontal para cada producto
                android.widget.LinearLayout itemLayout = new android.widget.LinearLayout(requireContext());
                itemLayout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 4, 0, 4);
                
                // Obtener datos del producto
                String nombreProducto = producto.optString("nombre", "Producto");
                int cantidad = producto.optInt("cantidad", 1);
                double precio = producto.optDouble("precio", 0);
                double subtotalProducto = precio * cantidad;
                total += subtotalProducto;
                
                // Texto del producto (cantidad x nombre)
                android.widget.TextView tvProducto = new android.widget.TextView(requireContext());
                tvProducto.setText(cantidad + "x " + nombreProducto);
                tvProducto.setTextColor(android.graphics.Color.DKGRAY);
                tvProducto.setMaxLines(1);
                tvProducto.setEllipsize(android.text.TextUtils.TruncateAt.END);
                tvProducto.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
                itemLayout.addView(tvProducto);
                
                // Precio total del ítem
                android.widget.TextView tvPrecio = new android.widget.TextView(requireContext());
                tvPrecio.setText(String.format(java.util.Locale.US, "$%.2f", subtotalProducto));
                tvPrecio.setTextColor(android.graphics.Color.DKGRAY);
                tvPrecio.setGravity(android.view.Gravity.END);
                tvPrecio.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                itemLayout.addView(tvPrecio);
                
                ticketLayout.addView(itemLayout);
            }
            
            // Si hay más productos, mostrar una indicación
            if (jsonArray.length() > 3) {
                android.widget.TextView tvMasProductos = new android.widget.TextView(requireContext());
                tvMasProductos.setText("+" + (jsonArray.length() - 3) + " productos más");
                tvMasProductos.setTextSize(12);
                tvMasProductos.setTypeface(null, android.graphics.Typeface.ITALIC);
                tvMasProductos.setTextColor(android.graphics.Color.DKGRAY);
                tvMasProductos.setGravity(android.view.Gravity.END);
                tvMasProductos.setPadding(0, 6, 0, 10);
                ticketLayout.addView(tvMasProductos);
            }
            
            // Actualizar el subtotal en SharedPreferences si calculamos un total válido
            if (total > 0) {
                prefs.edit().putString("subtotal", String.format(java.util.Locale.US, "%.2f", total)).apply();
                Log.d(TAG, "Subtotal actualizado según productos mostrados: " + total);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar productos en ticket: " + e.getMessage());
            // En caso de error, mostrar un mensaje informativo
            android.widget.TextView tvError = new android.widget.TextView(requireContext());
            tvError.setText("No se pudieron cargar los detalles de los productos");
            tvError.setTextColor(android.graphics.Color.GRAY);
            tvError.setTextSize(14);
            tvError.setPadding(0, 8, 0, 8);
            ticketLayout.addView(tvError);
        }
    }
    
    /**
     * Formatea un valor monetario para mostrarlo correctamente
     * @param valor Valor a formatear
     * @return Valor formateado
     */
    private String formatearValorMonetario(String valor) {
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

    /**
     * Formatea un valor monetario para mostrarlo correctamente (versión estática)
     * @param valor Valor a formatear
     * @return Valor formateado
     */
    public static String formatearValorMonetarioStatic(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "0.00";
        }
        
        try {
            // Eliminar cualquier caracter no numérico excepto el punto decimal
            String valorLimpio = valor.replaceAll("[^\\d.]", "");
            double valorDouble = Double.parseDouble(valorLimpio);
            return String.format(java.util.Locale.US, "%.2f", valorDouble);
        } catch (NumberFormatException e) {
            android.util.Log.e(TAG, "Error al formatear valor monetario: " + valor, e);
            return "0.00";
        }
    }    /**
     * Fija los valores de subtotal, envío y total en el ticket, elimina vistas previas si existen
     * @param ticketLayout Layout del ticket
     * @param subtotal Subtotal a mostrar
     * @param envio Costo de envío a mostrar
     * @param total Total a mostrar
     */
    private void fijarValoresTicket(ViewGroup ticketLayout, String subtotal, String envio, String total) {
        try {
            // Primero eliminar cualquier vista previa con las mismas tags
            for (String tag : new String[]{"subtotal_view", "envio_view", "total_view"}) {
                View existingView = ticketLayout.findViewWithTag(tag);
                if (existingView != null) {
                    ticketLayout.removeView(existingView);
                }
            }
            
            // Agregar separador visible antes de los valores monetarios
            android.view.View separador = new android.view.View(requireContext());
            separador.setBackgroundColor(android.graphics.Color.LTGRAY);
            separador.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1));
            separador.setPadding(0, 8, 0, 8);
            ticketLayout.addView(separador);
            
            // TextView para el subtotal - Desde el carrito
            android.widget.TextView tvSubtotal = new android.widget.TextView(requireContext());
            tvSubtotal.setId(android.view.View.generateViewId());
            tvSubtotal.setText("Subtotal: $ " + formatearValorMonetarioStatic(subtotal));
            tvSubtotal.setTextSize(15);
            tvSubtotal.setTextColor(android.graphics.Color.DKGRAY);
            tvSubtotal.setPadding(0, 12, 0, 4);
            tvSubtotal.setTag("subtotal_view");
            ticketLayout.addView(tvSubtotal);

            // TextView para el envío
            android.widget.TextView tvEnvio = new android.widget.TextView(requireContext());
            tvEnvio.setId(android.view.View.generateViewId());
            tvEnvio.setText("Envío: $ " + formatearValorMonetarioStatic(envio));
            tvEnvio.setTextSize(15);
            tvEnvio.setTextColor(android.graphics.Color.DKGRAY);
            tvEnvio.setPadding(0, 4, 0, 4);
            tvEnvio.setTag("envio_view");
            ticketLayout.addView(tvEnvio);

            // TextView para el total
            android.widget.TextView tvTotal = new android.widget.TextView(requireContext());
            tvTotal.setId(android.view.View.generateViewId());
            tvTotal.setText("Total: $ " + formatearValorMonetarioStatic(total));
            tvTotal.setTextSize(16);
            tvTotal.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTotal.setTextColor(android.graphics.Color.BLACK);
            tvTotal.setPadding(0, 8, 0, 8);
            tvTotal.setTag("total_view");
            ticketLayout.addView(tvTotal);
            
            // Guardar los valores en SharedPreferences para consistencia
            guardarSubtotalYEnvio(requireContext(), subtotal, envio);
            
            Log.d(TAG, "Valores fijados en el ticket - Subtotal: $" + subtotal + ", Envío: $" + envio + ", Total: $" + total);
        } catch (Exception e) {
            Log.e(TAG, "Error al fijar valores en ticket: " + e.getMessage(), e);
        }
    }
}
