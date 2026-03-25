const Reward = require('../models/Reward');

exports.getUserPoints = async (req, res) => {
    const { userId } = req.params;
    const reward = await Reward.findOne({ where: { UserId: userId } });
    res.json(reward || { points: 0 });
};

//exporting
exports.addPoints = async (req, res) => {
    const { userId, points } = req.body;
    let reward = await Reward.findOne({ where: { UserId: userId } });
    if (!reward) reward = await Reward.create({ UserId: userId, points });
    else reward.points += points, await reward.save();
    res.json(reward);
};
