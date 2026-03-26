const { DataTypes } = require('sequelize');
const sequelize = require('../config/db');
const User = require('./User');

const LostFound = sequelize.define('LostFound', {
    id: { type: DataTypes.INTEGER, primaryKey: true, autoIncrement: true },
    itemName: { type: DataTypes.STRING, allowNull: false },
    description: { type: DataTypes.TEXT },
    status: { type: DataTypes.ENUM('lost','found','returned'), defaultValue: 'lost' }
});
//---------lost and found--------
LostFound.belongsTo(User);

module.exports = LostFound;
