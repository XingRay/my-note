# JSON to MongoDB

[Get started free](https://www.mongodb.com/cloud/atlas/register)[Learn more about Atlas](https://www.mongodb.com/cloud/atlas)

JSON, or JavaScript Object Notation, has become a ubiquitous standard for representing structured data based on JavaScript object syntax. To make JSON an even better match for its innovative [document data model](https://www.mongodb.com/document-databases), MongoDB invented **BSON (Binary JSON)**, a binary representation to store data in JSON format, **optimized for speed, space, and flexibility**.

This tutorial will show you how to [import JSON data](https://docs.mongodb.com/guides/server/import/) into MongoDB for some key use cases.

## Table of Contents

- [Importing JSON in Linux](https://www.mongodb.com/compatibility/json-to-mongodb#how-to-import-json-into-mongodb-in-linux)
- [Importing JSON in Windows](https://www.mongodb.com/compatibility/json-to-mongodb#how-to-import-json-into-mongodb-in-windows)
- [Importing JSON using Java](https://www.mongodb.com/compatibility/json-to-mongodb#how-to-import-json-into-mongodb-using-java)
- [Importing JSON using Python](https://www.mongodb.com/compatibility/json-to-mongodb#how-to-import-json-into-mongodb-using-python)
- [FAQs](https://www.mongodb.com/compatibility/json-to-mongodb#relevant-faqs)

## Prerequisites

This tutorial assumes that you have installed and configured a MongoDB Atlas account and cluster. If you haven’t done so, here are a few useful steps:

- Ready a cluster or create one. You can create one MongoDB Atlas cluster for free. Learn more about MongoDB Atlas [here](https://www.mongodb.com/cloud/atlas).
- Set up a database along with a user and a password to import JSON documents.

## How to import JSON into MongoDB

MongoDB uses BSON to store data. While [JSON and BSON](https://www.mongodb.com/json-and-bson) have slight differences such as availability of data types and readability, they can easily be converted into each other.

The process to import JSON into MongoDB depends on the operating system and the programming language you are using. However, the key to importing is to access the MongoDB database and parsing the file that you want to import. You can then go through each document sequentially and insert into MongoDB. You can also choose to bulk import the file into MongoDB. Let’s learn how we can import JSON documents into MongoDB now.

## How to import JSON into MongoDB in Linux

To import JSON documents into MongoDB using Linux, open the terminal and install MongoDB tools. Run the following command from the terminal of your favorite Debian-based system, such as Ubuntu, to install MongoDB tools.

```
sudo apt install mongo-tools
```

After installing `mongo-tools`, you will have access to a series of CLI tools that can be used to interact with your MongoDB cluster. One of those tools is `mongoimport`, which can be used to import a JSON file into one of your collections. To do so, run the following command in your terminal.

```
mongoimport --uri 
mongodb+srv://<USERNAME>:<PASSWORD>@<CLUSTER_NAME>/<DATABASE> --collection <COLLECTION> --type json --file <FILENAME>
```

You can get the [connection string](https://docs.mongodb.com/guides/cloud/connectionstring/) to your cluster from the **Data Import and Export Tools** section located under **Command Line Tools** in your MongoDB Atlas cluster.

`<USERNAME>` and `<PASSWORD>` are the username and password of the database user and `<CLUSTER NAME>` refers to the cluster that holds the database.

`<DATABASE>` and `<COLLECTION>` refer to the name of the database and the collection, into which you want to import the JSON file

Finally, `<FILENAME>` is the full path and name of the JSON file you wish to import.

You can even import various other file formats such as TSV or CSV using mongoimport. Consult MongoDB’s official documentation on [mongoimport](https://docs.mongodb.com/manual/reference/program/mongoimport/) for more information.

## How to import JSON into MongoDB in Windows

To import a JSON document into MongoDB using Windows, download the [MongoDB database tools](https://www.mongodb.com/try/download/database-tools?tck=docs_databasetools). After the installation completes, you can use the mongoimport CLI tool to import JSON documents with the following command.

```
mongoimport --uri 
mongodb+srv://<USERNAME>:<PASSWORD>@<CLUSTER_NAME>/<DATABASE> --collection <COLLECTION> --type json --file <FILENAME>
```

Refer to the [section](https://www.mongodb.com/compatibility/json-to-mongodb#how-to-import-json-into-mongodb-in-linux) above for more information on the terminology used in this section.

Visit [mongoimport](https://docs.mongodb.com/manual/reference/program/mongoimport/) for more information.

## How to import JSON into MongoDB using Java

You can use a Java program, shown below, to import a JSON file into your MongoDB Atlas Cluster using Java and the [MongoDB Java Driver](https://mongodb.github.io/mongo-java-driver/). This program uses MongoDB Driver 4.3.0-beta 2. You can write this program on Intellij Idea and compile it using Java version 16.

**Note:** The code examples shown in this section require you to download maven dependencies. If your IDE doesn’t automatically download them for you, copy these dependencies into the pom.xml file.

```
...
    <properties>
        <maven.compiler.source>16</maven.compiler.source>
        <maven.compiler.target>16</maven.compiler.target>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongo-java-driver</artifactId>
            <version>3.7.0-rc0</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>2.10.0</version>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-nop</artifactId>
            <version>2.0.0-alpha1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongodb-driver-sync</artifactId>
            <version>4.3.0-beta2</version>
        </dependency>

    </dependencies>
...
```

After you have ensured the dependencies exist, run the following code from your favorite code editor.

```
import java.io.*;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import com.mongodb.client.MongoClients;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mongodb.MongoWriteException;
import org.apache.commons.io.IOUtils;
import org.bson.json.JsonObject;
import javax.naming.StringRefAddr;
 
public class mongo {
    public static void main(String[] args) throws IOException {

        com.mongodb.client.MongoClient client = MongoClients.create( "<CONNECTION STRING>");

        MongoDatabase database = client.getDatabase("<DATABASE>");
        MongoCollection<org.bson.Document> coll = database.getCollection("<COLLECTION>");

        try {

            //drop previous import
            coll.drop();

            //Bulk Approach:
            int count = 0;
            int batch = 100;
            List<InsertOneModel<Document>> docs = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new FileReader("<FILENAME>"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    docs.add(new InsertOneModel<>(Document.parse(line)));
                    count++;
                    if (count == batch) {
                        coll.bulkWrite(docs, new BulkWriteOptions().ordered(false));
                        docs.clear();
                        count = 0;
                    }
                }
            }

            if (count > 0) {
                BulkWriteResult bulkWriteResult=  coll.bulkWrite(docs, new BulkWriteOptions().ordered(false));
                System.out.println("Inserted" + bulkWriteResult);
            }

        } catch (MongoWriteException e) {
            System.out.println("Error");
        }

    }
}
```

Replace `<CONNECTION STRING>` with the [connection string](https://docs.mongodb.com/guides/cloud/connectionstring/) to your MongoDB database, `<DATABASE>` and `<COLLECTION>` with the name of the database and the collection, into which you want to import the JSON file and `<FILENAME>` with the full path and name of the JSON file.

In the above code, we read each line of the JSON file and insert one document at a time to an array list. This array list is then written into the database using the `bulkWrite` function. For more information on bulkWrite, you can visit the [documentation](https://docs.mongodb.com/manual/reference/method/db.collection.bulkWrite/) page.

You can find the entire implementation including the json file [on Github](https://github.com/mongodb-developer/MongoDB-Import-JSON-Java).

## How to import JSON into MongoDB using Python

To import JSON into MongoDB using Python, install [pymongo](https://www.mongodb.com/languages/python), the standard MongoDB driver library for Python, by running the following command in your terminal.

```
 pip3 install pymongo[srv]
```

Run the following code from a Python code editor to insert JSON into your MongoDB.

```
import pymongo
import json
from pymongo import MongoClient, InsertOne

client = pymongo.MongoClient(<CONNECTION STRING>)
db = client.<DATABASE>
collection = db.<COLLECTION>
requesting = []

with open(r"<FILENAME>") as f:
    for jsonObj in f:
        myDict = json.loads(jsonObj)
        requesting.append(InsertOne(myDict))

result = collection.bulk_write(requesting)
client.close()
```

Refer to the [section](https://www.mongodb.com/compatibility/json-to-mongodb#how-to-import-json-into-mongodb-using-java) above for more information on `<CONNECTION STRING>`, `<DATABASE>`, `<COLLECTION>` and `<FILENAME>`.

The above program loops through each document in the file and inserts it into a list. This list is then appended with the `InsertOne` function. After the loop reaches the end of the file, the program calls the `bulk_write` function to push all the documents to the collection at once.

**Note:** Because Python only escapes backslashes in a regular string, the name of the full path and name of the file to import is prepended with r in the code sample above. This way, that file becomes a raw string and Python doesn’t recognize the escape sequences.

You can find the entire implementation including the json file [on Github](https://github.com/mongodb-developer/MongoDB-Import-JSON-Python).