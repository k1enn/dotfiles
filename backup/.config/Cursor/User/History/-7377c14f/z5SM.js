import express from "express";
import dotenv from "dotenv";
import { connectDB } from "./config/db.js";
import productRoutes from "./routes/product.route.js";

dotenv.config();
const app = express();
app.use(express.json()); // Allow accepting JSON data in the request body

app.use("/api/products", productRoutes); // "api/products" is the prefix for the product routes

app.listen(5000, () => {
  connectDB();
  console.log("Server started at http://localhost:5000");
});
