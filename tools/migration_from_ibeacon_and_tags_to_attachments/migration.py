import argparse
import requests, json, base64


class CloudCommunication:
    def __init__(self, cloud_credentials):
        self.server_url = cloud_credentials.server_url
        self.app_id = cloud_credentials.app_id
        self.app_token = cloud_credentials.app_token

    def _req_headers(self):
        headers = {
            'accept': 'application/json',
            'authorization': 'Basic ' + base64.b64encode(self.app_id + ':' + self.app_token),
            'content-type': 'application/json',
            'cache-control': 'no-cache',
        }
        return headers

    def request_all_devices(self):
        url = '{0}/v2/devices'.format(self.server_url)

        headers = self._req_headers()
        response = requests.request('GET', url, headers=headers)
        if response.status_code == 200:
            return json.loads(response.text)
        else:
            response.raise_for_status()

    def add_attachment(self, attachment, device_id):
        exists, existing_attachment_id, existing_attachment = self._attachment_already_exists(device_id)
        if exists:
            attachment.update(existing_attachment)
            self._update_attachment(existing_attachment_id, attachment, device_id)
        else:
            self._add_new_attachment(attachment, device_id)

    def _add_new_attachment(self, attachment, device_identifier):
        url = '{0}/v3/attachments'.format(self.server_url)

        headers = self._req_headers()
        content = {
            'data': {
                'payload': attachment,
                'identifier': device_identifier,
                'for': 'device'
            }
        }
        content = json.dumps(content)
        response = requests.request('POST', url, data=content, headers=headers)
        if response.status_code == 200:
            print('Successfully assigned attachments {attachment} to device {identifier}'
                  .format(attachment=attachment, identifier=device_identifier))
        else:
            response.raise_for_status()

    def _attachment_already_exists(self, identifier):
        url = '{0}/v3/attachments'.format(self.server_url)

        headers = self._req_headers()
        params = {
            'identifiers': identifier
        }
        response = requests.request('GET', url, params=params, headers=headers)
        if response.status_code == 200:
            existing_attachments = response.json()['data']
            if len(existing_attachments) > 0:
                return True, existing_attachments[0]['id'], existing_attachments[0]['payload']
            else:
                return False, None, None
        else:
            response.raise_for_status()

    def _update_attachment(self, existing_attachment_id, attachment, device_identifier):
        url = '{0}/v3/attachments/{1}'.format(self.server_url, existing_attachment_id)

        headers = self._req_headers()
        content = {
            'data': {
                'payload': attachment,
            }
        }
        content = json.dumps(content)
        response = requests.request('PATCH', url, data=content, headers=headers)
        if response.status_code == 200:
            print('Successfully assigned attachments {attachment} to device {identifier}'
                  .format(attachment=attachment, identifier=device_identifier))
        else:
            response.raise_for_status()


def main():
    parser = argparse.ArgumentParser(description='Migrates attachment either from tags or iBeacon')
    parser.add_argument('app_id', help='your app id from cloud')
    parser.add_argument('app_token', help='your app token from cloud')
    parser.add_argument('--server_url', default='https://cloud.estimote.com',
                        help='url to Estimote Cloud')
    parser.add_argument('--no-ibeacon', action='store_true',
                        help='don\'t migrate iBeacon UUID/major/minor to attachments')

    args = parser.parse_args()
    estimote_cloud = CloudCommunication(args)
    print("Disabled iBeacon import = {0}".format(args.no_ibeacon))
    include_ibeacon = not args.no_ibeacon

    devices = estimote_cloud.request_all_devices()
    for device in devices:
        for tag in device['shadow']['tags']:
            if 'attachment' in tag:
                attachment = json.loads(tag)['attachment']
                estimote_cloud.add_attachment(attachment, device['identifier'])

        if include_ibeacon:
            ibeacon_settings = device['settings']['advertisers']['ibeacon'][0]
            if ibeacon_settings['enabled']:
                major = str(ibeacon_settings['major'])
                minor = str(ibeacon_settings['minor'])
                uuid = ibeacon_settings['uuid']
                attachment = {
                    'uuid': uuid,
                    'uuid:major': ':'.join([uuid, major]),
                    'uuid:major:minor': ':'.join([uuid, major, minor]),

                }
                estimote_cloud.add_attachment(attachment, device['identifier'])


if __name__ == '__main__':
    main()
