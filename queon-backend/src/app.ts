// src/app.ts
import express from "express";
import cors from "cors";
import examRoutes from "./routes/exam.routes";

const app = express();

// Middlewares
app.use(cors());
app.use(express.json());

// Mount exam routes at /api
console.log("🔗 Mounting /api routes");
app.use("/api", examRoutes);

app.get("/", (req, res) => {
  res.send("Queon backend API running ✔️");
});

export default app;
