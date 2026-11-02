import asyncio
import json
import aiohttp
from mg_ismart_india_client import MgIndiaClient

async def _connect(phone, password):
    async with aiohttp.ClientSession() as session:
        client = MgIndiaClient(session, phone=phone, password=password)
        await client.login()
        vehicles = await client.vehicles()
        if not vehicles:
            raise RuntimeError("MG login succeeded, but no vehicle was returned for this account.")
        vehicle = vehicles[0]
        client.vin = vehicle.vin
        status = await client.status(include_charge=True)
        def val(obj, key):
            value = getattr(obj, key, None)
            if value is None:
                return None
            if hasattr(value, "value"):
                return value.value
            return value
        charge = getattr(status, "charge", None)
        return {
            "ok": True,
            "vehicle": vehicle.name,
            "vin": vehicle.vin,
            "battery": val(status, "fuel_level"),
            "range_km": val(status, "range_km"),
            "odometer_km": val(status, "odometer_km"),
            "locked": val(status, "locked"),
            "climate_running": val(status, "climate_running"),
            "interior_temperature": val(status, "interior_temperature"),
            "charging": str(charge) if charge is not None else None
        }

def connect(phone, password):
    try:
        return json.dumps(asyncio.run(_connect(phone, password)))
    except Exception as exc:
        # Return only the server/client error message; never echo credentials.
        return json.dumps({"ok": False, "error": str(exc)[:400]})
