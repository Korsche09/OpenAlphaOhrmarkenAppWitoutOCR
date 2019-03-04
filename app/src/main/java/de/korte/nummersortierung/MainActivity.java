package de.korte.nummersortierung;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.korte.nummersortierung.db.InumberDb;
import de.korte.nummersortierung.db.Nummer;
import de.korte.nummersortierung.db.numberDB;

public class MainActivity extends AppCompatActivity {

    private EditText ohrMarkenNummer;
    private Button btnSend;
    private ListView listViewNumbers;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapterListView;
    private numberDB db;
    private Cursor cursorDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshAndSort();
        setContentView(R.layout.activity_main);
        ohrMarkenNummer = findViewById(R.id.editText);
        btnSend = findViewById(R.id.btnSetNummer);
        listViewNumbers = findViewById(R.id.lVOhrMarkenNummern);
        arrayList = new ArrayList<>();


        listViewNumbers.setAdapter(new ArrayAdapter<>());
        listViewNumbers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                final String text = listViewNumbers.getItemAtPosition(position).toString();
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Alarm");
                alertDialog.setMessage("Eintrag löschen?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                db.deleteFromTableWithNummer(text);
                                dialog.dismiss();
                                btnSortClick(view);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Nein",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater= getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return  super.onCreateOptionsMenu(menu);
    }

    public void btnSendClick(View view) {
        String number = ohrMarkenNummer.getText().toString();
        listViewNumbers.setAdapter(adapterListView);
        if(number.matches(".*[a-z].*"))
        {
            Toast.makeText(this, "Die Zahl beinhaltet Buchstaben bitte Versuche es erneut", Toast.LENGTH_LONG).show();
        }else if(arrayList.contains(number)){
            Toast.makeText(this, "Die Zahl ist bereits in der Liste vorhanden", Toast.LENGTH_LONG).show();
        } if (number.length()!=5 && number.length()!=10){
            Toast.makeText(this, "Die Zahl ist nicht 5 Ziffern lang bitte versuche es erneut", Toast.LENGTH_LONG).show();
        }
        else{
            db = new numberDB(this);
            ContentValues values = new ContentValues();
            values.put(InumberDb.Nummern,number);
            db.inserValues(values);
            arrayList.add(number);

        }
        ohrMarkenNummer.setText("");
        btnSortClick(view);

    }

    public void btnSortClick(View view) {
        refreshAndSort();
    }

    private void refreshAndSort() {
        db = new numberDB(this);
        cursorDB = db.GetAllData();

        String[] from =new String[]{InumberDb.Nummern};
        int[] to = new int[] {R.id.textView1};
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,R.layout.row_item, cursorDB,from,to) ;
        listViewNumbers.setAdapter(cursorAdapter);
        cursorAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursorDB.close();
        db.close();
    }

    public void btnResetClick(final View view) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alarm");
        alertDialog.setMessage("Datenbank zurücksetzen?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.deleteFromTable();
                        dialog.dismiss();
                        btnSortClick(view);
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Nein",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void btnSaveClick(final View view) throws FileNotFoundException {

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
        dialog.setTitle("Dateiname");
        final  EditText input = new EditText(this);
        dialog.setView(input);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
          new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                  try {
                      createPdf(input.getText().toString());
                  } catch (FileNotFoundException e) {
                      e.printStackTrace();
                  }
                  dialog.dismiss();
                  btnSortClick(view);
              }
          });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Nein",
          new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                  dialog.dismiss();
              }
          });
        dialog.show();

    }



    @TargetApi(Build.VERSION_CODES.N)
    private void createPdf(String fileName) throws FileNotFoundException {

        Document document = new Document();
        File file;
        try {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
            }
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),fileName+".pdf");
            System.err.println("'''''''''''''''''''''''''");
            System.err.println(file.getAbsolutePath());
            System.err.println("'''''''''''''''''''''''''");
            FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsoluteFile());
            PdfWriter.getInstance(document, fileOutputStream);
            document.open();
            for (Nummer zeile : db.getNummernAsList() ) {
                document.add(new Paragraph(zeile.getNumber()));
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }


        document.close();
    }
}
