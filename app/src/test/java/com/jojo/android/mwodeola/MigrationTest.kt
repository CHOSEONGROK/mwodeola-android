package com.jojo.android.mwodeola

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    // Array of all migrations
//    private val ALL_MIGRATIONS = arrayOf(UserAccountDB.MIGRATION_1_2)
//
//    @Rule
//    val helper: MigrationTestHelper = MigrationTestHelper(
//        InstrumentationRegistry.getInstrumentation(),
//        UserAccountDB::class.java.canonicalName,
//        FrameworkSQLiteOpenHelperFactory()
//    )
//
//    @Test
//    @Throws(IOException::class)
//    fun migrateAll() {
//        // Create earliest version of the database.
//        helper.createDatabase(TEST_DB, 1).apply {
//            close()
//        }
//
//        // Open latest version of the database. Room will validate the schema
//        // once all migrations execute.
//        Room.databaseBuilder(
//            InstrumentationRegistry.getInstrumentation().targetContext,
//            UserAccountDB::class.java,
//            TEST_DB
//        ).addMigrations(*ALL_MIGRATIONS).build().apply {
//            openHelper.writableDatabase
//            close()
//        }
//    }
}