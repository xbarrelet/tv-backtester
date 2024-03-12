import sys
from pprint import pprint

from pybit.unified_trading import HTTP


class BybitConnector:
    def __init__(self, api_key, api_secret):
        self.session = HTTP(
            testnet=False,
            api_key=api_key,
            api_secret=api_secret,
        )
        print(f"connector created with key:{api_key}")

    def get_available_funds(self):
        coin_balance = self.session.get_coin_balance(accountType='UNIFIED', coin='USDT')
        return float(coin_balance['result']['balance']['walletBalance'])

    def get_all_positions(self):
        positions: list[dict] = []

        is_next_trade_entry = True
        entry_price = 0
        last_balance = 0

        for position in self.session.get_transaction_log()['result']['list']:
            if is_next_trade_entry is True:
                is_next_trade_entry = False
                entry_price = position['tradePrice']
                last_balance = float(position['cashBalance'])

            else:
                is_next_trade_entry = True

                positions.append({
                    "symbol": position['symbol'].split("USDT")[0],
                    "side": "Buy" if position['side'] == "Sell" else "Sell",
                    "quantity": position['qty'],
                    "entry_price": entry_price,
                    "exit_price": position['tradePrice'],
                    "pnl": str(float(position['cashBalance']) - last_balance),
                    "fee": position['fee'],
                    "closing_timestamp": position['transactionTime']
                })

                    # Check why qty doesn't match what is displayed on Bybit dashboard, for now it's not guaranted exact

        return positions
