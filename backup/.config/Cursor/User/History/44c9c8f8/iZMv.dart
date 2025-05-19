class Recipe {
  String label;
  String imageUrl;

  Recipe(this.label, this.imageUrl);

  static List<Recipe> samples = [
    Recipe('Spaghetti and Meatballs', 'assets/2126711929_ef763de2b3_w.jpeg'),
    Recipe('Tomato Soup', 'assets/27729023535_a57606c1be.jpeg'),
    Recipe('Grilled Cheese', 'assets/3187380632_5056654a19_b.jpeg'),
    Recipe('Chocolate Chip Cookies', 'assets/15992102771_b92f4cc00a_b.jpeg'),
    Recipe('Taco Salad', 'assets/8533381643_a31a99e8a6_c.jpeg'),
    Recipe('Hawaiian Pizza', 'assets/15452035777_294cefced5_c.jpeg'),
  ];
}
