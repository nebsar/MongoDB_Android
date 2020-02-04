package com.nebi.mongodb

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.tasks.Task
import com.mongodb.stitch.android.core.Stitch
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient
import com.mongodb.stitch.core.services.mongodb.remote.ChangeEvent
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult
import org.bson.BsonValue
import org.bson.Document


class QueryActivity : AppCompatActivity() {

    val client = Stitch.getAppClient("first-android-ikpsh")

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_query)

        val textField: TextView = findViewById(R.id.query_text_view)

        val addButton: Button = findViewById(R.id.add_button)

        val updateButton: Button = findViewById(R.id.update_button)

        client.auth.user?.let {

            try {
                val mongoClient =
                    client.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas")

                val collection =
                    mongoClient.getDatabase("deneme-database").getCollection("deneme-collection")

                var count = 0


                val collectionTriggerWatch =
                    mongoClient.getDatabase("deneme-database").getCollection("trigger-collection")
                        .watch()


                updateButton.setOnClickListener {
                    val filterDoc = Document().append("userID", "5e29e79101e19fae0a831cd1")


                    val updateDoc = Document().append(
                        "\$set",
                        Document().append("GEO", count.toString()).append("AGE", "dndndsad").append(
                            "NOC2001",
                            "sdfsdf"
                        )
                    )

                    count += 1
//
                    val updateTask: Task<RemoteUpdateResult> = collection.updateOne(filterDoc, updateDoc)

                    updateTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val numMatched: Long = it.result.matchedCount
                            val numModified: Long = it.result.modifiedCount

                            Toast.makeText(
                                this, String.format(
                                    "successfully matched %d and modified %d documents",
                                    numMatched, numModified
                                ), Toast.LENGTH_LONG
                            ).show()



                        }
                    }




                }


                collectionTriggerWatch.addOnCompleteListener {

                    val result = it.result

                    result.addChangeEventListener { documentId: BsonValue?, event: ChangeEvent<Document?>? ->
                        event?.let {
                            val fullDocument = it.fullDocument!!


                            runOnUiThread {
                                textField.text = fullDocument["_id"].toString() + " is added"
                                Toast.makeText(this, fullDocument["_id"].toString()+ "is added", Toast.LENGTH_LONG).show()
                            }


                        }
                    }

                    result.setExceptionListener { documentId: BsonValue?, ex: Exception? ->

                        textField.text = ex!!.message.toString()

                    }
                }

                collectionTriggerWatch.addOnFailureListener {

                    textField.text = it.message.toString()
                }


                collectionTriggerWatch.addOnSuccessListener {
                    it.addChangeEventListener { documentId: BsonValue?, event: ChangeEvent<Document?>? ->
                        event?.let {
                            val fullDocument = it.fullDocument!!

                            runOnUiThread {
                                textField.text = fullDocument["_id"].toString() + " is added"
                                Toast.makeText(this, fullDocument["_id"].toString()+ "is added", Toast.LENGTH_LONG).show()
                            }

                        }
                    }
                }


                collectionTriggerWatch.addOnCanceledListener {
                    textField.text = it.toString()

                }


                addButton.setOnClickListener {
                    val newItem =
                        Document().append("userID", client.auth.user!!.id).append("GEO", "456489")
                            .append("Sex", "pppppppppppp").append("AGE", count.toString())
                            .append("NOC2001", "4444444")
                            .append("COWDO", "4444444")
                    count += 1

                    val insertTask = collection.insertOne(newItem)


                    insertTask.addOnCompleteListener {
                        if (it.isSuccessful) {

                            Toast.makeText(
                                this, String.format(
                                    "successfully inserted item with id %s",
                                    it.result.insertedId.toString()
                                ), Toast.LENGTH_LONG
                            ).show()

                        } else {
                            Toast.makeText(
                                this,
                                "failed to insert document with: " + it.exception,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }


//            val findResults = collection
//                .find(Document().append("userID","5e2939edc104eed67f40c761"))
//                //.projection(null)
//                .sort(Document().append("_id", 1))
//
//            val itemsTask = findResults.into(ArrayList<Document>())
//
//            itemsTask.addOnCompleteListener {
//                if (it.isSuccessful) {
//                    val items: List<Document> = it.result
//
//                    for (item in items) {
//
//                        textField.text = item["_id"].toString()+ " " + item["userID"].toString()+ " " + item["GEO"].toString()+" "+ item["Sex"].toString() +
//                                                                 " " + item["AGE"].toString() + item["NOC2001"].toString()+
//                                                                 " " + item["COWDO"].toString()
//                        Toast.makeText(this, item["_id"].toString()+" "+ item["GEO"].toString(), Toast.LENGTH_LONG).show()
//                }
//                } else {
//
//                }
//
//            }
//


                print("noldu")
            } catch (ex: Exception){
                print(ex.message.toString())
            }
        }
    }


//    private inner  class MyUpdateListener : ChangeEventListener<Document?> {
//        override fun onEvent(documentId: BsonValue, event: ChangeEvent<Document?>) {
//            if (!event.hasUncommittedWrites()) { // Custom actions can go here
//            }
//            // refresh the app view, etc.
//        }
//    }
//
//
//    private inner class MyErrorListener : ErrorListener {
//        override  fun onError(documentId: BsonValue?, error: Exception) {
//            Log.e("Stitch", error.localizedMessage)
//            val docsThatNeedToBeFixed: Set<BsonValue> =
//                _remoteCollection.sync().getPausedDocumentIds()
//            for (doc_id in docsThatNeedToBeFixed) { // Add your logic to inform the user.
//// When errors have been resolved, call
//                _remoteCollection.sync().resumeSyncForDocument(doc_id)
//            }
//            // refresh the app view, etc.
//        }
//    }
}
