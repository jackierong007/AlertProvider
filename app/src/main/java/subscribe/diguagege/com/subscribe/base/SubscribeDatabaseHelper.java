package subscribe.diguagege.com.subscribe.base;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hanwei on 16-7-25.
 */
public class SubscribeDatabaseHelper extends SQLiteOpenHelper {
    private static SubscribeDatabaseHelper mInstance;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "subscribe.db";

    public interface Tables {
        public static final String SUBSCRIBE = "Subscribe";
        public static final String SUBJECT = "Subject";
        public static final String REMINDER = "Reminders";
        public static final String ALERTS = "SubscribeAlerts";
        public static final String LINKED = "Linked";
    }

    private DatabaseUtils.InsertHelper mSubjectInserter;
    private DatabaseUtils.InsertHelper mSubscribeInserter;
    private DatabaseUtils.InsertHelper mReminderInserter;
    private DatabaseUtils.InsertHelper mAlertsInserter;
    private DatabaseUtils.InsertHelper mLinkedInserter;

    public long insertSubject(ContentValues values) {
        return mSubjectInserter.insert(values);
    }

    public long insertSubscribe(ContentValues values) {
        return mSubscribeInserter.insert(values);
    }

    public long insertReminders(ContentValues values) {
        return mReminderInserter.insert(values);
    }

    public long insertAlerts(ContentValues values) {
        return mAlertsInserter.insert(values);
    }

    public long insertLinked(ContentValues values) {
        return mLinkedInserter.insert(values);
    }

    public SubscribeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized SubscribeDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SubscribeDatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        bootstrapDB(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        mSubjectInserter = new DatabaseUtils.InsertHelper(db, Tables.SUBJECT);
        mSubscribeInserter = new DatabaseUtils.InsertHelper(db, Tables.SUBSCRIBE);
        mReminderInserter = new DatabaseUtils.InsertHelper(db, Tables.REMINDER);
        mAlertsInserter = new DatabaseUtils.InsertHelper(db, Tables.ALERTS);
        mLinkedInserter = new DatabaseUtils.InsertHelper(db, Tables.LINKED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void bootstrapDB(SQLiteDatabase db) {
        createSubjectsTable(db);
        createSubscribesTable(db);
        createReminder(db);
        createSubscribeAlerts(db);
        createLinked(db);
        // Trigger to remove data tied to an event when we delete that event.
        db.execSQL("CREATE TRIGGER subject_cleanup_delete DELETE ON " + Tables.SUBJECT + " " +
                "BEGIN " +
                SUBJECT_CLEANUP_TRIGGER_SQL +
                "END");

        db.execSQL("CREATE TRIGGER linked_cleanup_delete DELETE ON " + Tables.LINKED + " " +
                "BEGIN " +
                LINKED_CLEANUP_TRIGGER_SQL +
                "END");

        db.execSQL("CREATE TRIGGER subjects_cleanup_delete DELETE ON " + Tables.SUBSCRIBE + " " +
                "BEGIN " +
                SUBSCRIBE_CLEANUP_TRIGGER_SQL +
                "END");
    }

//**************************************分割线*************************************************
    // This needs to be done when all the tables are already created
    private static final String SUBSCRIBE_CLEANUP_TRIGGER_SQL =
                    "DELETE FROM " + Tables.REMINDER +
                    " WHERE " + SubscribeContract.Reminders.SUBSCRIBE_ID + "=" +
                    "old." + SubscribeContract.Subscribe._ID + ";" +
                    "DELETE FROM " + Tables.ALERTS +
                    " WHERE " + SubscribeContract.SubscribeAlerts.SUBSCRIBE_ID + "=" +
                    "old." + SubscribeContract.Subscribe._ID + ";";

    private static final String SUBJECT_CLEANUP_TRIGGER_SQL =
            "DELETE FROM " + Tables.LINKED +
                    " WHERE " + SubscribeContract.Linked.SUBJECT_ID + "=" +
                    "old." + SubscribeContract.Subject._ID + ";";

    private static final String LINKED_CLEANUP_TRIGGER_SQL =
            "DELETE FROM " + Tables.SUBSCRIBE +
                    " WHERE 1=(SELECT " + SubscribeContract.Subscribe.SUBJECT_COUNT +
                    " FROM " + Tables.SUBSCRIBE +
                    " WHERE " + SubscribeContract.Subscribe._ID + "=" +
                    "old." + SubscribeContract.Linked.SUBSCRIBE_ID + " );" +
            "UPDATE " + Tables.SUBSCRIBE + " SET " +
                    SubscribeContract.Subscribe.SUBJECT_COUNT + "=(SELECT " + SubscribeContract.Subscribe.SUBJECT_COUNT +
                    " FROM " + Tables.SUBSCRIBE +
                    " WHERE " + SubscribeContract.Subscribe._ID + "=" +
                    "old." + SubscribeContract.Linked.SUBSCRIBE_ID
                    + " )-1" +
                    " WHERE " + SubscribeContract.Subscribe._ID + "=" +
                    "old." + SubscribeContract.Linked.SUBSCRIBE_ID + ";";



    private void createSubscribesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.SUBSCRIBE + " (" +
                SubscribeContract.Subscribe._ID + " INTEGER PRIMARY KEY," +
                SubscribeContract.Subscribe.SUBJECT_ID  + " INTEGER," +
                SubscribeContract.Subscribe.SUBJECT_COUNT + " INTEGER," +
                SubscribeContract.Subscribe.TITLE + " TEXT," +
                SubscribeContract.Subscribe.ACTION + " TEXT," +
                SubscribeContract.Subscribe.DESCRIPTION + " TEXT," +
                SubscribeContract.Subscribe.DTSTART + " INTEGER," +
                SubscribeContract.Subscribe.DTEND + " INTEGER," +
                SubscribeContract.Subscribe.ORGINZER + " TEXT," +
                SubscribeContract.Subscribe.ICON_URL + " TEXT," +
                SubscribeContract.Subscribe.EXTEND_DATA1 + " TEXT," +
                SubscribeContract.Subscribe.EXTEND_DATA2 + " TEXT," +
                SubscribeContract.Subscribe.EXTEND_DATA3 + " TEXT," +
                SubscribeContract.Subscribe.EXTEND_DATA4 + " TEXT"
                + " )");
    }

    private void createSubjectsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.SUBJECT + " (" +
                SubscribeContract.Subject._ID + " INTEGER PRIMARY KEY," +
                SubscribeContract.Subject.TITLE + " TEXT," +
                SubscribeContract.Subject.ACTION + " TEXT," +
                SubscribeContract.Subject.DESCRIPTION + " TEXT," +
                SubscribeContract.Subject.ICON_URL + " TEXT," +
                SubscribeContract.Subject.TYPE + " INTEGER"
                + " )");
    }

    private void createReminder(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.REMINDER + " (" +
                SubscribeContract.Reminders._ID + " INTEGER PRIMARY KEY," +
                SubscribeContract.Reminders.SUBSCRIBE_ID + " INTEGER," +
                SubscribeContract.Reminders.MINUTES + " INTEGER"
                + " )");
    }

    private void createSubscribeAlerts(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ALERTS + " (" +
                SubscribeContract.SubscribeAlerts._ID + " INTEGER PRIMARY KEY," +
                SubscribeContract.SubscribeAlerts.SUBSCRIBE_ID + " INTEGER," +
                SubscribeContract.SubscribeAlerts.BEGIN + " INTEGER," +
                SubscribeContract.SubscribeAlerts.END + " INTEGER," +
                SubscribeContract.SubscribeAlerts.ALARM_TIME + " INTEGER," +
                SubscribeContract.SubscribeAlerts.CREATION_TIME + " INTEGER," +
                SubscribeContract.SubscribeAlerts.RECEIVED_TIME + " INTEGER," +
                SubscribeContract.SubscribeAlerts.NOTIFY_TIME + " INTEGER," +
                SubscribeContract.SubscribeAlerts.STATE + " INTEGER," +
                SubscribeContract.SubscribeAlerts.MINUTES + " INTEGER"
                + " )");
    }

    private void createLinked(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.LINKED + " (" +
                SubscribeContract.Linked._ID + " INTEGER PRIMARY KEY," +
                SubscribeContract.Linked.SUBJECT_ID + " INTEGER," +
                SubscribeContract.Linked.SUBSCRIBE_ID + " INTEGER" +
                " )");
    }

}
