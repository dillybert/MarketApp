import firebase_admin
from firebase_admin import credentials, firestore
import random
import time

cred = credentials.Certificate("market-7edc6-firebase-adminsdk-jk88j-1e1efd3244.json")
firebase_admin.initialize_app(cred)

db = firestore.client()

def seed_products(count: int):
    collection = db.collection("products")
    for i in range(1, count + 1):
        product = {
            "barcode": f"barcode_{i}",
            "name": f"Product {i}",
            "quantity": random.randint(0, 1000),
            "ownPrice": round(random.uniform(10, 500), 2),
            "price": round(random.uniform(10, 500), 2),
            "supplier": f"Supplier {i}",
            "unit": random.choice(["кг", "шт", "л"]),
            "updatedAt": firestore.SERVER_TIMESTAMP,
            "createdAt": firestore.SERVER_TIMESTAMP
        }
        doc_ref = collection.document(product["barcode"])
        doc_ref.set(product)
        if i % 100 == 0:
            print(f"Inserted {i} products...")
    print(f"✅ Done! Inserted {count} products.")

if __name__ == "__main__":
    count = int(input("How many products to insert? "))
    start = time.time()
    seed_products(count)
    print(f"⏱️ Time elapsed: {time.time() - start:.2f} seconds")

