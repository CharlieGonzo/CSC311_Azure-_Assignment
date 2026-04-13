package com.example.module03_basicgui_db_interface.database;

import com.example.module03_basicgui_db_interface.Person;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseHandlerTest {

    static DatabaseHandler db;
    static Person person;
    @BeforeAll
    static void setUp() {
        db = new DatabaseHandler();
        db.connectToDatabase();
    }

    @Test
    @Order(1)
    void insertUser() {
        assertTrue(db.insertUser("test","test","test","test"));
    }

    @Test
    @Order(2)
    void getUserByName() throws InterruptedException {
        Thread.sleep(2000);
        person = db.queryUserByName("test");
        assertEquals("test", person.getFirstName());
    }

    @Test
    @Order(3)
    void updateUser() throws InterruptedException {
        assertTrue(db.updateUser(person.getId(),"changed",person.getLastName(),person.getDept(),person.getMajor()));

    }

    @Test
    @Order(4)
    void removeUser(){
        assertTrue(db.removeUser(person.getId()));
    }


}