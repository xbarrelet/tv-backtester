import time
from pprint import pprint

from pybit.unified_trading import HTTP


def current_milli_time():
    return round(time.time() * 1000)


class BybitConnector:
    def __init__(self, api_key, api_secret):
        self.session = HTTP(
            testnet=False,
            api_key=api_key,
            api_secret=api_secret,
        )

    def get_available_funds(self):
        coin_balance = self.session.get_coin_balance(accountType='UNIFIED', coin='USDT')
        return float(coin_balance['result']['balance']['walletBalance'])

    def get_all_positions(self):
        positions: list[dict] = []

        # first_timestamp = 1725185826000
        # current_timestamp = current_milli_time()
        # seven_days_interval = 604800000
        #
        # while first_timestamp < current_timestamp:
        pnls = self.session.get_closed_pnl(category='linear')['result']['list']
        # first_timestamp += seven_days_interval

        for pnl in pnls:
            positions.append({
                "symbol": pnl['symbol'].split("USDT")[0],
                "side": "Buy" if pnl['side'] == "Sell" else "Sell",
                "quantity": pnl['qty'],
                "entry_price": pnl['avgEntryPrice'],
                "exit_price": pnl['avgExitPrice'],
                "pnl": pnl['closedPnl'],
                "closing_timestamp": pnl['updatedTime'],
                "leverage": pnl['leverage']
            })

        return sorted(positions, key=lambda x: x['closing_timestamp'], reverse=True)

    def get_latest_price_for_symbol(self, symbol):
        ticket_result = self.session.get_tickers(category="linear", symbol=symbol)
        latest_close = float(ticket_result['result']['list'][0]['lastPrice'])
        return latest_close

    def get_current_position(self):
        orders = self.session.get_positions(category='linear')['result']['list']

        if len(orders) == 1:
            return {
                "symbol": orders[0]['symbol'].split("USDT")[0],
                "side": orders[0]['side'],
                "unrealisedPnl": orders[0]['unrealisedPnl'],
                "entry_price": orders[0]['avgPrice'],
                "leverage": orders[0]['leverage'],
                "current_price": self.get_latest_price_for_symbol(orders[0]['symbol']),
                "timestamp": orders[0]['createdTime']
            }
        elif len(orders) > 1:
            print("ERROR, more than 1 position opened!")
        else:
            return None
