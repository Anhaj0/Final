const Bus = require('../models/Bus');

exports.updateLocation = async (req, res) => {
    const { busId, lat, lng } = req.body;
    try {
        const bus = await Bus.findByPk(busId);
        if (!bus) return res.status(404).json({ message: 'Bus not found' });

        bus.location = { lat, lng };
        await bus.save();
        res.json({ message: 'Location updated', bus });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
//_________Export________
exports.getAllBuses = async (req, res) => {
    const buses = await Bus.findAll();
    res.json(buses);
};
