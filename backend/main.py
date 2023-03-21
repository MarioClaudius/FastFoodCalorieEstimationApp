from fastapi import FastAPI
import uvicorn

app = FastAPI()

@app.get('/')
def test():
    return {"Hello": "World"}


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    uvicorn.run(app)

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
