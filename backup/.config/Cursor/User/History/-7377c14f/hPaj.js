import express from "express";
import dotenv from "dotenv";
import { connectDB } from "./config/db.js";
import Product from "./models/product.model.js";

dotenv.config();
const app = express();
app.use(express.json()); // Allow accepting JSON data in the request body

// POST request to create a product
app.post("/api/products", async (req, res) => {
  const product = req.body; // User will send a request to the server

  if (!product.name || !product.price || !product.image) {
    return res
      .status(400)
      .json({ success: false, message: "Please provide all fields" });
  }

  const newProduct = new Product(product);
  try {
    await newProduct.save();
    res.status(201).json({ success: true, data: newProduct });
  } catch (error) {
    console.log("Error in creating product", error.message);
    res.status(500).json({ success: false, message: error.message });
  }
});

// DELETE request to delete a product
app.delete("/api/products/:id", async (req, res) => {
  const { id } = req.params;
  try {
    await Product.findByIdAndDelete(id);
    res.status(200).json({ success: true, message: "Product deleted" });
  } catch (error) {
    console.log(
      "Error in deleting product or product doesn't exist",
      error.message
    );
    res.status(500).json({ success: false, message: error.message });
  }
});

//GET request to get all products
app.get("/api/products", async (req, res) => {
  try {
    const products = await Product.find({}); // Fetch all products from the database
    res.status(200).json({ success: true, data: products });
  } catch (error) {
    console.log("Error in fetching products", error.message);
    res.status(500).json({ success: false, message: error.message });
  }
});

// PUT request to update a product
app.put("/api/products/:id", async (req, res) => {
  const { id } = req.params;
  const product = req.body;
  try {
    const updatedProduct = await Product.findByIdAndUpdate(id, product, { new: true });
    res.status(200).json({ success: true, message: "Product updated" });
  } catch (error) {
    console.log("Error in updating product", error.message);
    res.status(500).json({ success: false, message: error.message });
  }
});
app.listen(5000, () => {
  connectDB();
  console.log("Server started at http://localhost:5000");
});
