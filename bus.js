const { DataTypes } = require('sequelize');
const sequelize = require('../config/db');
const User = require('./User');
//addinng
const Bus = sequelize.define('Bus', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    busNumber: { type: DataTypes.STRING, allowNull: false },
    route: { type: DataTypes.STRING, allowNull: false },
    location: { type: DataTypes.JSONB, defaultValue: { lat: 0, lng: 0 } }
});
//bus
Bus.belongsTo(User, { as: 'driver', foreignKey: 'driverId' });

module.exports = Bus;
