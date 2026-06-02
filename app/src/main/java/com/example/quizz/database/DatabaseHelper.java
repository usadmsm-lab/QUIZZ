package com.example.quizz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quizz.models.Question;
import com.example.quizz.models.Quiz;
import com.example.quizz.models.Result;
import com.example.quizz.models.User;
import com.example.quizz.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        db.execSQL("CREATE TABLE " + Constants.TABLE_USERS + " (" +
                Constants.USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Constants.USER_EMAIL + " TEXT UNIQUE," +
                Constants.USER_PASSWORD + " TEXT," +
                Constants.USER_NAME + " TEXT)");

        // Create Quizzes table
        db.execSQL("CREATE TABLE " + Constants.TABLE_QUIZZES + " (" +
                Constants.QUIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Constants.QUIZ_NAME + " TEXT," +
                Constants.QUIZ_DESC + " TEXT," +
                Constants.QUIZ_QUESTION_COUNT + " INTEGER," +
                Constants.QUIZ_TIME_LIMIT + " INTEGER)");

        // Create Questions table
        db.execSQL("CREATE TABLE " + Constants.TABLE_QUESTIONS + " (" +
                Constants.QUESTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Constants.QUESTION_QUIZ_ID + " INTEGER," +
                Constants.QUESTION_TEXT + " TEXT," +
                Constants.QUESTION_ANSWER_A + " TEXT," +
                Constants.QUESTION_ANSWER_B + " TEXT," +
                Constants.QUESTION_ANSWER_C + " TEXT," +
                Constants.QUESTION_ANSWER_D + " TEXT," +
                Constants.QUESTION_CORRECT + " TEXT," +
                "FOREIGN KEY(" + Constants.QUESTION_QUIZ_ID + ") REFERENCES " +
                Constants.TABLE_QUIZZES + "(" + Constants.QUIZ_ID + "))");

        // Create Results table
        db.execSQL("CREATE TABLE " + Constants.TABLE_RESULTS + " (" +
                Constants.RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Constants.RESULT_USER_ID + " INTEGER," +
                Constants.RESULT_QUIZ_ID + " INTEGER," +
                Constants.RESULT_SCORE + " INTEGER," +
                Constants.RESULT_TOTAL + " INTEGER," +
                Constants.RESULT_CORRECT + " INTEGER," +
                Constants.RESULT_WRONG + " INTEGER," +
                Constants.RESULT_TIMESTAMP + " LONG," +
                "FOREIGN KEY(" + Constants.RESULT_USER_ID + ") REFERENCES " +
                Constants.TABLE_USERS + "(" + Constants.USER_ID + ")," +
                "FOREIGN KEY(" + Constants.RESULT_QUIZ_ID + ") REFERENCES " +
                Constants.TABLE_QUIZZES + "(" + Constants.QUIZ_ID + "))");

        // Insert sample data
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_RESULTS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_QUESTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_QUIZZES);
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USERS);
        onCreate(db);
    }

    // User operations
    public boolean registerUser(String email, String password, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.USER_EMAIL, email);
        values.put(Constants.USER_PASSWORD, password);
        values.put(Constants.USER_NAME, name);
        long result = db.insert(Constants.TABLE_USERS, null, values);
        return result != -1;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_USERS,
                null,
                Constants.USER_EMAIL + " = ? AND " + Constants.USER_PASSWORD + " = ?",
                new String[]{email, password},
                null, null, null);

        if (cursor.moveToFirst()) {
            User user = new User(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
            );
            cursor.close();
            return user;
        }
        cursor.close();
        return null;
    }

    // Quiz operations
    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_QUIZZES, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Quiz quiz = new Quiz(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        cursor.getInt(4)
                );
                quizzes.add(quiz);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return quizzes;
    }

    public Quiz getQuizById(int quizId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_QUIZZES,
                null,
                Constants.QUIZ_ID + " = ?",
                new String[]{String.valueOf(quizId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            Quiz quiz = new Quiz(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4)
            );
            cursor.close();
            return quiz;
        }
        cursor.close();
        return null;
    }

    // Question operations
    public List<Question> getQuestionsByQuizId(int quizId) {
        List<Question> questions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_QUESTIONS,
                null,
                Constants.QUESTION_QUIZ_ID + " = ?",
                new String[]{String.valueOf(quizId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Question question = new Question(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7)
                );
                questions.add(question);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return questions;
    }

    // Result operations
    public long addResult(int userId, int quizId, int score, int totalQuestions, 
                         int correctAnswers, int wrongAnswers) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.RESULT_USER_ID, userId);
        values.put(Constants.RESULT_QUIZ_ID, quizId);
        values.put(Constants.RESULT_SCORE, score);
        values.put(Constants.RESULT_TOTAL, totalQuestions);
        values.put(Constants.RESULT_CORRECT, correctAnswers);
        values.put(Constants.RESULT_WRONG, wrongAnswers);
        values.put(Constants.RESULT_TIMESTAMP, System.currentTimeMillis());
        return db.insert(Constants.TABLE_RESULTS, null, values);
    }

    public List<Result> getUserResults(int userId) {
        List<Result> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(Constants.TABLE_RESULTS,
                null,
                Constants.RESULT_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, Constants.RESULT_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Result result = new Result(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        cursor.getInt(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        cursor.getLong(7)
                );
                results.add(result);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return results;
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert sample quizzes
        ContentValues quizValues = new ContentValues();
        
        quizValues.put(Constants.QUIZ_NAME, "Bài Test 1");
        quizValues.put(Constants.QUIZ_DESC, "Kiểm tra kiến thức cơ bản");
        quizValues.put(Constants.QUIZ_QUESTION_COUNT, 10);
        quizValues.put(Constants.QUIZ_TIME_LIMIT, 30);
        db.insert(Constants.TABLE_QUIZZES, null, quizValues);

        quizValues.clear();
        quizValues.put(Constants.QUIZ_NAME, "Bài Test 2");
        quizValues.put(Constants.QUIZ_DESC, "Kiểm tra kiến thức nâng cao");
        quizValues.put(Constants.QUIZ_QUESTION_COUNT, 15);
        quizValues.put(Constants.QUIZ_TIME_LIMIT, 45);
        db.insert(Constants.TABLE_QUIZZES, null, quizValues);

        // Insert sample questions
        ContentValues questionValues = new ContentValues();
        
        questionValues.put(Constants.QUESTION_QUIZ_ID, 1);
        questionValues.put(Constants.QUESTION_TEXT, "1 + 1 = ?");
        questionValues.put(Constants.QUESTION_ANSWER_A, "1");
        questionValues.put(Constants.QUESTION_ANSWER_B, "2");
        questionValues.put(Constants.QUESTION_ANSWER_C, "3");
        questionValues.put(Constants.QUESTION_ANSWER_D, "4");
        questionValues.put(Constants.QUESTION_CORRECT, "B");
        db.insert(Constants.TABLE_QUESTIONS, null, questionValues);

        questionValues.clear();
        questionValues.put(Constants.QUESTION_QUIZ_ID, 1);
        questionValues.put(Constants.QUESTION_TEXT, "2 + 2 = ?");
        questionValues.put(Constants.QUESTION_ANSWER_A, "2");
        questionValues.put(Constants.QUESTION_ANSWER_B, "3");
        questionValues.put(Constants.QUESTION_ANSWER_C, "4");
        questionValues.put(Constants.QUESTION_ANSWER_D, "5");
        questionValues.put(Constants.QUESTION_CORRECT, "C");
        db.insert(Constants.TABLE_QUESTIONS, null, questionValues);
    }
}
