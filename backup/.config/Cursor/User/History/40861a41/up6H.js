export const getProducts = async (req, res) => {
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