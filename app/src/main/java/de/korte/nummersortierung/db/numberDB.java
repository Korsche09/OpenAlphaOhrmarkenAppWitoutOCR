package de.korte.nummersortierung.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class numberDB extends SQLiteOpenHelper {

    public static final String NAME = "number.db";
    public static final int VERSION = 1;

    public numberDB(Context context){
        super(context, NAME,null, VERSION);
    }

    public Cursor GetAllData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT DISTINCT * FROM OhrmarkenNummern ORDER BY CAST(Nummern AS Interger) ASC ;", null);
        return c;
    }

    public void inserValues(ContentValues values){

        onCreate(this.getWritableDatabase());
        SQLiteDatabase db =this.getWritableDatabase();
        long id =db.insert(InumberDb.TABLENAME,null,values);
        db.close();
    }

    public void deleteFromTable(){
       SQLiteDatabase db = this.getWritableDatabase();
       db.execSQL("DELETE FROM OhrmarkenNummern");

    }

    public void deleteFromTableWithNummer(String number){
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM OhrmarkenNummern WHERE Nummern="+number);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS "+InumberDb.TABLENAME +"( ");
        sb.append(InumberDb._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        sb.append(InumberDb.Nummern +" VARCHAR);");

        sqLiteDatabase.execSQL(sb.toString());

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+NAME);
        onCreate(sqLiteDatabase);
    }

    public List<Nummer> getNummernAsList(){
        List<Nummer> nummerList = new ArrayList<>();
        String query = "SELECT "+InumberDb.Nummern +" FROM " + InumberDb.TABLENAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor =  db.rawQuery(query,null);

        if(cursor.moveToFirst()){
            do {
                Nummer nummer = new Nummer();
                nummer.setNumber(cursor.getString(0));
                nummerList.add(nummer);
            } while (cursor.moveToNext());
        }
        return nummerList;
    }



}
