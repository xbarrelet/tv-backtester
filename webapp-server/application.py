import os
from pprint import pprint

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
                                                            os.getenv(f"SUB{sub_accounts_counter}_SECRET")))

    return connectors_from_env_variables


connectors: list[BybitConnector] = initialize_connectors()

test_connector = connectors[0]
pprint(test_connector.get_all_positions())


# app = Flask(__name__)


# @app.route("/")
# def hello_world():
#     return jsonify({"message": "Hello, World!"})
#
#
# if __name__ == "__main__":
#     app.run(debug=True)
