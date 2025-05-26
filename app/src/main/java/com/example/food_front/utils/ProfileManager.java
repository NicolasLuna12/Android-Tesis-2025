package com.example.food_front.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ProfileManager {
    private static final String PREF_NAME = "user_info";
    private static final String KEY_NAME = "nombre";
    private static final String KEY_SURNAME = "apellido";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "telefono";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public ProfileManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Guardar los datos del usuario
    public void saveInfo(String name, String surname, String email, String phone) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_SURNAME, surname);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.apply();
        Log.d("auth", "Nombre guardado despues del login:" + name );
        Log.d("auth", "Apellido guardado despues del login:" + surname );
        Log.d("auth", "Email guardado despues del login:" + email );
        Log.d("auth", "Telefono guardado despues del login:" + phone );
    }

    // Leer los datos del usuario
    public String getInfo() {
        return sharedPreferences.getString(PREF_NAME, null);
    }

    public String getName() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public String getSurname() {
        return sharedPreferences.getString(KEY_SURNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    // Borrar la session
    public void clearInfo() {
        editor.clear();
        editor.apply();
    }
}