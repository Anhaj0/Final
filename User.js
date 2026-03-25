const { DataTypes } = require('sequelize');
const sequelize = require('../config/db');
//adding

const User = sequelize.define('User', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    name: { type: DataTypes.STRING, allowNull: false },
    email: { type: DataTypes.STRING, allowNull: false, unique: true },
    role: { type: DataTypes.ENUM('passenger','driver','admin'), defaultValue: 'passenger' }
});

module.exports = User;
// Updated by Amasha
