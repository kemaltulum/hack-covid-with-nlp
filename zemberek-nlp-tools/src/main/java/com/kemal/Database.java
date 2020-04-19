package com.kemal;

import java.util.HashMap;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

public class Database {
	MongoClient connection;
	MongoDatabase database;
	
	public Database() {
		
		// Connect mongodb on localhost 27017
		this.connection = MongoClients.create("mongodb://localhost:27017");
		this.database = this.connection.getDatabase("covid_hack");

	}
	
	public void getAllFromCollection(String collection) {
		
	}
	
}
