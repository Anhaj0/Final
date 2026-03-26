//______ Rewards _________
const { DataTypes } = require('sequelize');
const sequelize = require('../config/db');
const User = require('./User');

const Reward = sequelize.define('Reward', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    points: { type: DataTypes.INTEGER, defaultValue: 0 }
});

Reward.belongsTo(User);

module.exports = Reward;
//