package com.example.android.sendsms;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
public class MainActivity extends AppCompatActivity {
    /*
    Código del mensaje de envío y
    Uri de contenido global
     */
    public static final int PICK_CONTACT_REQUEST = 1 ;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Uri contactUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void initPickContacts(View v){
        /*
        Crear un intent para seleccionar un contacto del dispositivo
         */
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        /*
        Iniciar la actividad esperando respuesta a través
        del canal PICK_CONTACT_REQUEST
         */
        startActivityForResult(i, PICK_CONTACT_REQUEST);
    }

    private void renderContact(Uri uri) {

        /*
        Obtener instancias de los Views
         */
        TextView contactName = (TextView)findViewById(R.id.contactName);
        TextView contactPhone = (TextView)findViewById(R.id.contactPhone);
        ImageView contactPic = (ImageView)findViewById(R.id.contactPic);

        /*
        Setear valores
         */
        contactName.setText(getName(uri));
        contactPhone.setText(getPhone(uri));
        contactPic.setImageBitmap(getPhoto(uri));
        String contact = getName(uri);
        Log.d(TAG,"EEEEYYYY  " + String.valueOf(contact));

    }

    public void sendMessage(View v){

        /*
        Creando nuestro gestor de mensajes
         */
        SmsManager smsManager = SmsManager.getDefault();

        /*
        Enviando el mensaje
         */
        if(contactUri!=null) {
            smsManager.sendTextMessage(
                    getPhone(contactUri),
                    null,
                    "¡Estamos aprendiendo a Desarrollar en Android!",
                    null,
                    null);

            Toast.makeText(this, "Mensaje Enviado", Toast.LENGTH_LONG).show();
        }else
            Toast.makeText(this, "Selecciona un contacto primero", Toast.LENGTH_LONG).show();


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                /*
                Capturar el valor de la Uri
                 */
                contactUri = intent.getData();
                /*
                Procesar la Uri
                 */
                renderContact(contactUri);
            }
        }
    }

    private String getPhone(Uri uri) {
        /*
        Variables temporales para el id y el teléfono
         */
        String id = null;
        String phone = null;

        /************* PRIMERA CONSULTA ************/
        /*
        Obtener el _ID del contacto
         */
        Cursor contactCursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null);


        if (contactCursor.moveToFirst()) {
            id = contactCursor.getString(0);
        }
        contactCursor.close();

        /************* SEGUNDA CONSULTA ************/
        /*
        Sentencia WHERE para especificar que solo deseamos
        números de telefonía móvil
         */
        String selectionArgs =
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE+"= " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;

        /*
        Obtener el número telefónico
         */
        Cursor phoneCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
                selectionArgs,
                new String[] { id },
                null
        );
        if (phoneCursor.moveToFirst()) {
            phone = phoneCursor.getString(0);
        }
        phoneCursor.close();

        return phone;
    }

    private String getName(Uri uri) {

        /*
        Valor a retornar
         */
        String name = null;

         /*
        Obtener una instancia del Content Resolver
         */
        ContentResolver contentResolver = getContentResolver();

        /*
        Consultar el nombre del contacto
         */
        Cursor c = contentResolver.query(
                uri,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                null,
                null,
                null);


        if(c.moveToFirst()){
            name = c.getString(0);
        }

        /*
        Cerramos el cursor
         */
        c.close();

        return name;
    }

    private Bitmap getPhoto(Uri uri) {
        /*
        Foto del contacto y su id
         */
        Bitmap photo = null;
        String id = null;

        /************* CONSULTA ************/
        Cursor contactCursor = getContentResolver().query(
                uri,
                new String[]{ContactsContract.Contacts._ID},
                null,
                null,
                null);

        if (contactCursor.moveToFirst()) {
            id = contactCursor.getString(0);
        }
        contactCursor.close();

        /*
        Usar el método de clase openContactPhotoInputStream()
         */
        try {
            Uri contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    Long.parseLong(id));

            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                    getContentResolver(),
                    contactUri);

            if (input != null) {
                /*
                Dar formato tipo Bitmap a los bytes del BLOB
                correspondiente a la foto
                 */
                photo = BitmapFactory.decodeStream(input);
                input.close();
            }

        } catch (IOException iox) { /* Manejo de errores */ }

        return photo;
    }
}
