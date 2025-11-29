console.log("📦 Queon server bootstrap starting...");

import app from "./app";

const PORT = process.env.PORT ? Number(process.env.PORT) : 4000;

app.listen(PORT, () => {
  console.log(`🚀 Queon backend running on port ${PORT}`);
});
