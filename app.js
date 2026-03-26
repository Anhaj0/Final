const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const sequelize = require('./config/db');

const app = express();
app.use(cors());
app.use(bodyParser.json());
//adding

const userRoutes = require('./routes/userRoutes');
const busRoutes = require('./routes/busRoutes');
const rewardRoutes = require('./routes/rewardRoutes');
const complaintRoutes = require('./routes/complaintRoutes');
const lostFoundRoutes = require('./routes/lostFoundRoutes');

app.use('/api/users', userRoutes);
app.use('/api/buses', busRoutes);
app.use('/api/rewards', rewardRoutes);
app.use('/api/complaints', complaintRoutes);
app.use('/api/lostfound', lostFoundRoutes);

sequelize.sync({ alter: true }).then(() => {
    const PORT = process.env.PORT || 5000;
    app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
});

// ===== Debug Utility Functions (Added by Amasha) =====

function logInfo(message) {
  console.log("[INFO]:", message);
}

function logWarning(message) {
  console.log("[WARNING]:", message);
}

function logError(message) {
  console.log("[ERROR]:", message);
}

// Test logs
logInfo("Application started");
logWarning("This is a sample warning");
logError("This is a sample error");

// Utility function
function calculateSum(a, b) {
  return a + b;
}

console.log("Sum:", calculateSum(5, 10));