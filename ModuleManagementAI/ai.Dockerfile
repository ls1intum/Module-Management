FROM python:3.11-slim

WORKDIR /code

COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

COPY ./app /code/app

COPY ./data/module-info /code/data/module-info

COPY ./data/cache /code/data/cache

VOLUME ["/code/data"]

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "5000"]