import asyncio, json, aiohttp
from mg_ismart_india_client import MgIndiaClient, hash_control_pin

def payload(vehicle, status):
    c = status.charge
    return {"ok": True, "vehicle": vehicle.name, "vin": vehicle.vin,
        "battery": status.fuel_level, "range_km": status.range_km,
        "odometer_km": status.odometer_km, "locked": status.locked,
        "climate_running": status.climate_running,
        "interior_temperature": status.interior_temperature,
        "charging": None if c is None else {"is_charging": c.is_charging,
        "is_plugged_in": c.is_plugged_in, "soc": c.soc,
        "power_kw": c.charging_power_kw, "remaining_min": c.charge_time_remaining_min}}

async def session_for(phone, password, vin=None):
    s = aiohttp.ClientSession()
    c = MgIndiaClient(s, phone=phone, password=password, vin=vin)
    try:
        await c.login()
        vehicles = await c.vehicles()
        if not vehicles: raise RuntimeError("No vehicle is linked to this MG account.")
        v = c.vehicle or vehicles[0]
        if vin and v.vin != vin: raise RuntimeError("Selected vehicle VIN did not match the MG account.")
        c.vin = v.vin
        return s, c, v
    except Exception:
        await s.close()
        raise

async def _connect(phone, password):
    s,c,v = await session_for(phone,password)
    try: return payload(v, await c.status(include_charge=True))
    finally: await s.close()

async def _control(phone,password,pin,vin,action):
    s,c,v = await session_for(phone,password,vin)
    try:
        c.pin_hash = hash_control_pin(pin)
        if action == "lock": await c.control_door_lock(True)
        elif action == "unlock": await c.control_door_lock(False)
        elif action == "climate_on": await c.control_climate(True)
        elif action == "climate_off": await c.control_climate(False)
        else: raise RuntimeError("Unsupported command.")
        st = await c.status(include_charge=True)
        verified = {"lock": st.locked is True, "unlock": st.locked is False,
                    "climate_on": st.climate_running is True,
                    "climate_off": st.climate_running is False}[action]
        result = payload(v,st)
        result.update({"verified":verified,"command":action})
        if not verified: result["message"] = "Command sent, but fresh vehicle status did not confirm it. Check the official MG app before retrying."
        return result
    finally: await s.close()

def run(coro):
    try: return json.dumps(asyncio.run(coro))
    except Exception as e:
        msg = str(e)[:300]
        if "did not complete" in msg.lower() or "indeterminate" in msg.lower():
            msg = "MG did not confirm whether the command completed. Check the official MG app or vehicle before retrying."
        return json.dumps({"ok":False,"error":msg})

def connect(phone,password): return run(_connect(phone,password))
def control(phone,password,pin,vin,action): return run(_control(phone,password,pin,vin,action))
