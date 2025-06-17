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
    private String fecha;
    private String nroPedido;
    private String metodoPago;
    private String total;
    private String subtotal;
    private String envio;

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
    }

    private String productos;

    @Nullable
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
        View view = inflater.inflate(R.layout.fragment_ticket_detail, container, false);

        ((TextView) view.findViewById(R.id.tvFecha)).setText("Fecha: " + fecha);
        ((TextView) view.findViewById(R.id.tvNro)).setText("N° Pedido: " + nroPedido);
        ((TextView) view.findViewById(R.id.tvPago)).setText("Método de pago: " + metodoPago);
        ((TextView) view.findViewById(R.id.tvProductos)).setText(productos);
        // Mostrar subtotal y envío en el ticket extendido
        ((TextView) view.findViewById(R.id.tvSubtotal)).setText("Subtotal: $" + subtotal);
        ((TextView) view.findViewById(R.id.tvEnvio)).setText("Envío: $" + envio);
        ((TextView) view.findViewById(R.id.tvTotal)).setText("Total: $" + total);

        Button btnCerrar = view.findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Detalles del Ticket");
        return dialog;
    }
}
