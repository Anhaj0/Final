const { DataTypes } = require('sequelize');
const sequelize = require('../config/db');
const User = require('./User');
const Bus = require('./bus');

//complaint

const Complaint = sequelize.define('Complaint', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    message: { type: DataTypes.TEXT, allowNull: false },
    status: { type: DataTypes.ENUM('pending','resolved'), defaultValue: 'pending' }
});

Complaint.belongsTo(User);
Complaint.belongsTo(Bus);

module.exports = Complaint;
