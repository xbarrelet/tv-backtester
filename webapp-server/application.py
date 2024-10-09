import os
from pprint import pprint

from dotenv import load_dotenv
from flask import Flask, jsonify, request

from bybit_connector import BybitConnector

load_dotenv()


def initialize_connectors():
    connectors_from_env_variables = []

    # sub_accounts_counter = -1
    # while os.getenv(f"SUB{sub_accounts_counter + 1}_KEY") is not None:
    #     sub_accounts_counter += 1
    #     connectors_from_env_variables.append(BybitConnector(os.getenv(f"SUB{sub_accounts_counter}_KEY"),
    #                                                         os.getenv(f"SUB{sub_accounts_counter}_SECRET")))
    connectors_from_env_variables.append(BybitConnector(os.getenv("SUB0_KEY"), os.getenv("SUB0_SECRET")))
    return connectors_from_env_variables


connectors: list[BybitConnector] = initialize_connectors()
app = Flask(__name__)


@app.route("/positions")
def get_all_positions():
    positions = {}

    for connector in connectors:
        current_position = connector.get_current_position()
        positions[connector.session.api_key] = current_position

    return jsonify(positions)


@app.route("/pnls")
def get_all_closed_pnls():
    positions = {}

    for connector in connectors:
        current_position = connector.get_all_positions()
        positions[connector.session.api_key] = current_position

    return jsonify(positions)

# {
#   "simplified":"1",
#   "inverse": "0",
#  "unified":"1",
#   "encryptor": "1",
#   "api_key": "wQFHId6bRUiY9KDUt3uB6tAdFbFDEp8rK8GmPyk848E=",
#   "secret_key": "nEwiICyYWki0mLoe4fPSLtvp6TNv2Rkqbzm3EU9JkBL/eA9zBsT1mFXotwvKquKo",
#   "passphrase": "ABCDEFG",
#   "email_id": "x.barrelet@gmail.com",
#   "margin_mode": "1", "use_testnet": "0",  "qty_in_percentage": "0", "order_type":"reduce_short", "position": "6", "leverage":"10", "sell_leverage":"10", "buy_leverage":"10",    "exit_existing_trade": "0","tp_type": "0","enable_multi_tp" : "1","pyramiding": "0","stop_bot_below_balance": "", "channelName": "",  "telegram_bot": "0",
#   "action":"buy",
#   "qty":"758.214",
#   "position_size":"0",
#   "price":"134.17",
#   "signal_param":"{}",
#   "signal_type":"",
#   "coin_pair":"SOLUSDT.P",
#   "time":"2024-09-12T01:16:02Z"
# }

@app.route("/trade", methods=["POST"])
def open_trade():
    json_payload = request.json
    return jsonify({})


if __name__ == "__main__":
    app.run(debug=True)
