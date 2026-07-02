from flask import Flask, Response, request, render_template

app = Flask(__name__, template_folder='templates')  # designates this script as the root path


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['POST', 'GET'])
def login():
    if request.method == "POST":
        # get username and pass from form inputs 
        username = request.form.get('username')
        password = request.form.get('password')
        if (username == "admin" and password == "su"):
            resp = Response("", status=301, mimetype="application/text")
            resp.headers["Location"] = "/buy"
            resp.headers["Set-Cookie"] = f"session=mysession; Path=/"
            return resp
        return Response("wrong", status=400, mimetype="application/text")
    elif request.method == "GET":
        return render_template('login.html')
    else:
        return Response("wrong", status=400, mimetype="application/text")

@app.route('/buy', methods=['POST', 'GET'])
def buy():
    if request.method == "POST":
        print(request.data)
        if request.cookies.get("session") == "mysession":
            try:
                data = request.get_json(force=True)
                item = data.get('item')
                quantity = data.get('quantity')
                assert(item in ["item1", "item2", "item3"])
                assert(int(quantity) > 0)
                response = Response(f"bought {quantity} {item} successfully", status=200, mimetype="application/text")
                response.headers["Location"] = "/"
                return response
            except Exception as e:
                return Response("faulure", status=401, mimetype="application/text")
        else:
            return Response("not logged in", status=401, mimetype="application/text")
    elif request.method == "GET":
        return render_template('buy.html')
    else:
        return Response("wrong", status=400, mimetype="application/text")

   


if __name__ == "__main__":  # if running this file directly
    app.run(debug=True, port=3000)  # run the app
    # app.run(host="0.0.0.0", debug=True, port=3000)  # expose to all ips
