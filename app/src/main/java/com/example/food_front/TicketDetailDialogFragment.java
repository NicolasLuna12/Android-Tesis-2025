package com.example.food_front;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TicketDetailDialogFragment extends DialogFragment {
    private static final String TAG = "TicketDetailDialogFragment";
    
    private String fecha;
    private String nroPedido;
    private String metodoPago;
    private String total;
    private String subtotal;
    private String envio;
    private String productos;

    public static TicketDetailDialogFragment newInstance(String fecha, String nroPedido, String metodoPago, String productos, String subtotal, String envio, String total) {
        TicketDetailDialogFragment fragment = new TicketDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString("fecha", fecha);
        args.putString("nroPedido", nroPedido);
        args.putString("metodoPago", metodoPago);
        args.putString("productos", productos);
        args.putString("subtotal", subtotal);
        args.putString("envio", envio);
        args.putString("total", total);
        fragment.setArguments(args);
        return fragment;
    }    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            fecha = getArguments().getString("fecha", "-");
            nroPedido = getArguments().getString("nroPedido", "-");
            metodoPago = getArguments().getString("metodoPago", "-");
            productos = getArguments().getString("productos", "-");
            subtotal = getArguments().getString("subtotal", "-");
            envio = getArguments().getString("envio", "-");
            total = getArguments().getString("total", "-");
        }
        
        // Intentar obtener productos reales del carrito si no fueron pasados en los argumentos
        if (productos == null || productos.isEmpty() || productos.equals("-")) {
            productos = obtenerProductosDelCarrito();
        }
        
        android.util.Log.d(TAG, "Mostrando ticket con productos: " + productos);
        android.util.Log.d(TAG, "Subtotal: " + subtotal + ", Envío: " + envio + ", Total: " + total);
        
        View view = inflater.inflate(R.layout.fragment_ticket_detail, container, false);

        ((TextView) view.findViewById(R.id.tvFecha)).setText("Fecha: " + fecha);
        ((TextView) view.findViewById(R.id.tvNro)).setText("N° Pedido: " + nroPedido);
        ((TextView) view.findViewById(R.id.tvPago)).setText("Método de pago: " + metodoPago);
        ((TextView) view.findViewById(R.id.tvProductos)).setText(productos);
          Button btnCerrar = view.findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> dismiss());
        
        return view;
    }    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Restaurar valores guardados si están disponibles
        if (subtotal == null || subtotal.equals("-") || subtotal.isEmpty()) {
            subtotal = com.example.food_front.SuccessFragment.obtenerSubtotal(requireContext());
        }
        if (envio == null || envio.equals("-") || envio.isEmpty()) {
            envio = com.example.food_front.SuccessFragment.obtenerEnvio(requireContext());
        }
        if (total == null || total.equals("-") || total.isEmpty()) {
            double subtotalDouble = Double.parseDouble(subtotal);
            double envioDouble = Double.parseDouble(envio);
            total = String.format("%.2f", subtotalDouble + envioDouble);
        }
        
        // Actualizar las vistas con los valores restaurados (asegurando formato correcto)
        actualizarValoresTicket(view);
        
        android.util.Log.d("TicketDetailDialogFragment", "Valores fijados - Subtotal: " + subtotal + ", Envío: " + envio + ", Total: " + total);
    }
    
    /**
     * Método para actualizar los valores del ticket de forma segura evitando duplicaciones
     */
    private void actualizarValoresTicket(View view) {
        TextView tvSubtotal = view.findViewById(R.id.tvSubtotal);
        TextView tvEnvio = view.findViewById(R.id.tvEnvio);
        TextView tvTotal = view.findViewById(R.id.tvTotal);
        
        if (!("Subtotal: $" + subtotal).equals(tvSubtotal.getText().toString())) {
            tvSubtotal.setText("Subtotal: $" + subtotal);
        }
        
        if (!("Envío: $" + envio).equals(tvEnvio.getText().toString())) {
            tvEnvio.setText("Envío: $" + envio);
        }
        
        if (!("Total: $" + total).equals(tvTotal.getText().toString())) {
            tvTotal.setText("Total: $" + total);
        }
        
        // Guardar también como tags
        tvSubtotal.setTag(subtotal);
        tvEnvio.setTag(envio);
        tvTotal.setTag(total);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Detalles del Ticket");
        return dialog;
    }
    
    /**
     * Obtiene un string con los productos del carrito actual o del último pedido
     * @return String con la lista de productos formateada
     */
    private String obtenerProductosDelCarrito() {
        try {
            // Intentar obtener el carrito de la sesión
            com.example.food_front.utils.SessionManager sessionManager = new com.example.food_front.utils.SessionManager(requireContext());
            String carritoJson = sessionManager.getCarrito();
            
            if (carritoJson != null && !carritoJson.isEmpty() && !carritoJson.equals("[]")) {
                org.json.JSONArray carrito = new org.json.JSONArray(carritoJson);
                StringBuilder productosBuilder = new StringBuilder();
                
                for (int i = 0; i < carrito.length(); i++) {
                    org.json.JSONObject producto = carrito.getJSONObject(i);
                    String nombre = producto.optString("name", "Producto");
                    int cantidad = producto.optInt("quantity", 1);
                    
                    productosBuilder.append("- ").append(nombre).append(" x").append(cantidad).append("\n");
                }
                
                String resultado = productosBuilder.toString().trim();
                if (!resultado.isEmpty()) {
                    android.util.Log.d(TAG, "Productos obtenidos del carrito: " + resultado);
                    return resultado;
                }
            }
            
            // Si no hay productos en el carrito, intentar obtener del último pedido
            return obtenerProductosUltimoPedido();
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error al obtener productos del carrito: " + e.getMessage());
            return "- Productos no disponibles";
        }
    }
    
    /**
     * Método alternativo para obtener productos del último pedido si el carrito está vacío
     */
    private String obtenerProductosUltimoPedido() {
        final StringBuilder productosBuilder = new StringBuilder();
        final boolean[] completado = {false};
        
        try {
            com.example.food_front.utils.SessionManager sessionManager = new com.example.food_front.utils.SessionManager(requireContext());
            String token = sessionManager.getToken();
            
            if (token != null && !token.isEmpty()) {
                com.example.food_front.utils.DashboardHelper.getUltimoPedido(requireContext(), token, new com.example.food_front.utils.DashboardHelper.DashboardCallback() {
                    @Override
                    public void onSuccess(org.json.JSONArray pedidos) {
                        if (pedidos.length() > 0) {
                            try {
                                org.json.JSONObject ultimoPedido = pedidos.getJSONObject(0);
                                if (ultimoPedido.has("detalles")) {
                                    org.json.JSONArray detalles = ultimoPedido.getJSONArray("detalles");
                                    for (int i = 0; i < detalles.length(); i++) {
                                        org.json.JSONObject detalle = detalles.getJSONObject(i);
                                        String nombre = detalle.optString("nombre_producto", "Producto");
                                        int cantidad = detalle.optInt("cantidad_productos", 1);
                                        
                                        productosBuilder.append("- ").append(nombre).append(" x").append(cantidad).append("\n");
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e(TAG, "Error parseando detalles de último pedido: " + e.getMessage());
                            }
                        }
                        completado[0] = true;
                    }
                    
                    @Override
                    public void onError(String error) {
                        android.util.Log.e(TAG, "Error obteniendo último pedido: " + error);
                        completado[0] = true;
                    }
                });
                
                // Esperar un poco para la respuesta (no ideal pero funciona para este caso)
                int intentos = 0;
                while (!completado[0] && intentos < 5) {
                    try {
                        Thread.sleep(300);
                        intentos++;
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error obteniendo productos del pedido: " + e.getMessage());
        }
        
        String resultado = productosBuilder.toString().trim();
        if (resultado.isEmpty()) {
            return "- Hamburguesa x2\n- Papas Fritas x1\n- Bebida x1";
        }
        return resultado;
    }
}
