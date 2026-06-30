from flask import Flask, Response, request

app = Flask(__name__)  # designates this script as the root path

COOKIE_NAME = "session"
COOKIE_VALUE = "authenticated"


@app.route('/')
def index():
    return Response("hello world", status=201, mimetype="application/text")


@app.route('/login')
def login():
    if (request.args.get('username') == "admin"
            and request.args.get('password') == "su"):
        resp = Response("", status=301, mimetype="application/text")
        resp.headers["Location"] = "/adminpage"
        resp.headers["Set-Cookie"] = f"{COOKIE_NAME}={COOKIE_VALUE}; Path=/"
        return resp
    return Response("wrong", status=400, mimetype="application/text")


@app.route('/adminpage')
def adminpage():
    if request.cookies.get(COOKIE_NAME) == COOKIE_VALUE:
        return Response("welcome admin", status=200, mimetype="application/text")
    return Response("wrong", status=400, mimetype="application/text")


if __name__ == "__main__":  # if running this file directly
    app.run(debug=True, port=3000)  # run the app
    # app.run(host="0.0.0.0", debug=True, port=3000)  # expose to all ips
