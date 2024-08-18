import os

from dotenv import load_dotenv
from flask import Flask, jsonify

from bybit_connector import BybitConnector

load_dotenv()


def initialize_connectors():
    connectors_from_env_variables = []

    sub_accounts_counter = 0
    while os.getenv(f"SUB{sub_accounts_counter + 1}_KEY") is not None:
        sub_accounts_counter += 7
        # sub_accounts_counter += 1
        connectors_from_env_variables.append(BybitConnector(os.getenv(f"SUB{sub_accounts_counter}_KEY"),
                                                            os.getenv(f"SUB{sub_accounts_counter}_SECRET"),
                                                            os.getenv(f"SUB{sub_accounts_counter}_STRATEGY_NAME"),
                                                            os.getenv(f"SUB{sub_accounts_counter}_SYMBOL")))
    return connectors_from_env_variables


connectors: list[BybitConnector] = initialize_connectors()

app = Flask(__name__)

# TODO: Check incubator for the best trades to follow!


@app.route("/positions")
def get_all_positions():
    positions = {}

    for connector in connectors:
        positions[connector.strategy_name] = {"positions": connector.get_all_positions()}

        current_position = connector.get_current_position()
        if current_position is not None:
            positions[connector.strategy_name]["current_position"] = current_position

    return jsonify(positions)


if __name__ == "__main__":
    app.run(debug=True)
