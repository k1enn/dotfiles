import express from "express";
import dotenv from "dotenv";
import { connectDB } from "./config/db.js";
dotenv.config();
const app = express();

// API endpoint to get all products
app.get("/products", async (req, res) => {
  const products = req.body; // User will send a request to the server

  if (!products.name || !products.price || !products.image) {
    return res.status(400).json({success: false, message: "Please provide all fields" });
  }

  const newProduct = new Product(product);
  try {
    await newProduct.save();
    res.status(201).json({success: true, data: newProduct});
  } catch (error) {
    console.log("Error in creating product", error.message);
    res.status(500).json({success: false, message: error.message});
  }
});

app.listen(5000, () => {
  connectDB();
  console.log("Server started at http://localhost:5000");
});