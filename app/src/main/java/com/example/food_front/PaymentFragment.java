package com.example.food_front;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class PaymentFragment extends Fragment {

    private RadioGroup radioGroupPaymentMethod;
    private RadioButton radioButtonCreditCard, radioButtonPayPal;
    private Button buttonNext;
    private static final String PAYMENT_PREFS = "payment_preferences";
    private static final String SELECTED_PAYMENT_METHOD = "selected_payment_method";
    private static final String TAG = "PaymentFragment";

    public PaymentFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Inicializar vistas
        radioButtonCreditCard = view.findViewById(R.id.radioButton4);
        radioButtonPayPal = view.findViewById(R.id.radioButton6);
        buttonNext = view.findViewById(R.id.button2);

        // Aquí puedes dejar la lógica que desees para el botón, pero sin MercadoPago
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireContext(), "Funcionalidad de pago deshabilitada", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
