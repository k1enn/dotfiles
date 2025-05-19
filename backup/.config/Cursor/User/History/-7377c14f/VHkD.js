import express from "express";
import dotenv from "dotenv";
import { connectDB } from "./config/db.js";
dotenv.config();
const app = express();

// API endpoint to get all products
app.get("/products", (req, res) => {
  const products = req.body; // User will send a request to the server
});

app.listen(5000, () => {
  connectDB();
  console.log("Server started at http://localhost:5000");
});