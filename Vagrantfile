# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|

  config.vm.box = "ubuntu/bionic64"

  config.vm.provider "virtualbox" do |vb|
     vb.memory = "4096"
  end
  
  config.vm.provision "shell", privileged: true, path: 'provision-root-priviledged.sh'
  config.vm.provision "shell", privileged: false, path: 'provision-user-priviledged.sh'

  config.vm.provision "Copy user's git config", type:'file', source: '~/.gitconfig', destination: '.gitconfig'
  
end
