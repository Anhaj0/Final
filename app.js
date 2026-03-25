const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const sequelize = require('./config/db');

const app = express();
app.use(cors());
app.use(bodyParser.json());

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