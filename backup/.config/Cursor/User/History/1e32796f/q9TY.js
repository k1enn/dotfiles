import express from "express";

const router = express.Router();

export default router;

// POST request to create a product
router.post("/", async (req, res) => {
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
router.delete("/:id", async (req, res) => {
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
router.get("/", async (req, res) => {
  try {
    const products = await Product.find({}); // Fetch all products from the database
    res.status(200).json({ success: true, data: products });
  } catch (error) {
    console.log("Error in fetching products", error.message);
    res.status(500).json({ success: false, message: error.message });
  }
});

// PUT request to update a product
router.put("/:id", async (req, res) => {
  const { id } = req.params;
  const product = req.body;

  if (!mongoose.Types.ObjectId.isValid(id)) {
    return res
      .status(404)
      .json({ success: false, message: "Invalid product ID" });
  }

  try {
    const updatedProduct = await Product.findByIdAndUpdate(id, product, {
      new: true,
    });
    res
      .status(200)
      .json({
        success: true,
        data: updatedProduct,
        message: "Product updated",
      });
  } catch (error) {
    console.log("Error in updating product", error.message);
    res.status(500).json({ success: false, message: error.message });
  }
});
